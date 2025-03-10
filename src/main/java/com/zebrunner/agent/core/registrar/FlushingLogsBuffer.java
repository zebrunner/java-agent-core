package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.domain.Test;

/**
 * Effectively acts as an in-memory buffer for logs generated in scope of test run that is meant to reduce
 * number of outgoing requests issued to Zebrunner
 * <p>The {@code scheduleFlush} method schedules logs transfer with configurable delay.
 *
 * @param <E> the type of the input to the log event converter function
 */
@Slf4j
final class FlushingLogsBuffer<E> implements LogsBuffer<E> {

    private static final ScheduledExecutorService FLUSH_EXECUTOR = Executors.newScheduledThreadPool(4);
    private static final ZebrunnerApiClient API_CLIENT = ApiClientRegistry.getClient();
    private static final AtomicBoolean EXECUTOR_ENABLED = new AtomicBoolean();

    private static volatile Queue<Log> QUEUE = new ConcurrentLinkedQueue<>();
    private static final ThreadLocal<Queue<Log>> CONFIGURATION_LOGS_QUEUE = new ThreadLocal<>();
    private final Function<E, Log> converter;

    FlushingLogsBuffer(Function<E, Log> converter) {
        this.converter = converter;
        Runtime.getRuntime().addShutdownHook(new Thread(FlushingLogsBuffer::shutdown));
        ReportingRegistrar.registerLogsBuffer(this);
    }

    @Override
    public void put(E event) {
        Optional<Test> currentTest = ReportingContext.getCurrentTest();
        Log log = converter.apply(event);
        if (currentTest.isPresent()) {
            log.setTestId(String.valueOf(currentTest.get().getId()));
            QUEUE.add(log);
        } else {
            if (CONFIGURATION_LOGS_QUEUE.get() == null) {
                CONFIGURATION_LOGS_QUEUE.set(new ConcurrentLinkedQueue<>());
            }
            CONFIGURATION_LOGS_QUEUE.get().add(log);
        }
        // lazily enables buffer and schedules flushes on the very first event to be buffered
        if (EXECUTOR_ENABLED.compareAndSet(false, true)) {
            scheduleFlush();
        }
    }

    @Override
    public void flushQueuedConfigurationLogs() {
        ReportingContext.getCurrentTest()
                        .ifPresent(currentTest -> {
                            Queue<Log> queue = CONFIGURATION_LOGS_QUEUE.get();
                            if (queue != null) {
                                while (!queue.isEmpty()) {
                                    Log log = queue.poll();
                                    log.setTestId(String.valueOf(currentTest.getId()));
                                    QUEUE.add(log);
                                }
                                CONFIGURATION_LOGS_QUEUE.remove();
                            }
                        });
    }

    @Override
    public void clearQueuedConfigurationLogs() {
        CONFIGURATION_LOGS_QUEUE.remove();
    }

    private static void scheduleFlush() {
        FLUSH_EXECUTOR.scheduleWithFixedDelay(FlushingLogsBuffer::flush, 1, 1, TimeUnit.SECONDS);
    }

    private static void flush() {
        if (!QUEUE.isEmpty()) {
            Queue<Log> logsBatch = QUEUE;
            QUEUE = new ConcurrentLinkedQueue<>();

            Long testRunId = ReportingContext.getNullableTestRunId();
            if (testRunId != null) {
                API_CLIENT.sendLogs(logsBatch, testRunId);
            }
        }
    }

    private static void shutdown() {
        FLUSH_EXECUTOR.shutdown();
        try {
            if (!FLUSH_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                FLUSH_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        FlushingLogsBuffer.flush();
    }

}
