package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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
    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();
    private static final AtomicBoolean EXECUTOR_ENABLED = new AtomicBoolean();

    private static Queue<Log> QUEUE = new ConcurrentLinkedQueue<>();
    private final Function<E, Log> converter;

    /** Allocates a new {@code LogsBuffer} object bound to certain logging framework event type.
     * <p>Theoretically (but unlikely) more than one logging framework may be used in a single test project.
     *
     * @param converter logging framework specific event converter
     */
    FlushingLogsBuffer(Function<E, Log> converter) {
        this.converter = converter;
        Runtime.getRuntime().addShutdownHook(new Thread(FlushingLogsBuffer::shutdown));
    }

    /**
     * Inserts specified event to the queue
     * @param event log event
     */
    @Override
    public void put(E event) {
        Log log = converter.apply(event);
        log.setTestId(RunContext.getCurrentTest().getZebrunnerId());

        QUEUE.add(log);

        // lazily enables buffer and schedules flushes on the very first event to be buffered
        if (EXECUTOR_ENABLED.compareAndSet(false, true)) {
            scheduleFlush();
        }
    }

    private static void scheduleFlush() {
        FLUSH_EXECUTOR.scheduleWithFixedDelay(FlushingLogsBuffer::flush, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized static void flush() {
        if (!QUEUE.isEmpty()) {
            String testRunId = RunContext.getRun().getZebrunnerId();
            Queue<Log> logsBatch = QUEUE;
            QUEUE = new ConcurrentLinkedQueue<>();
            API_CLIENT.sendLogs(logsBatch, testRunId);
        }
    }

    private static void shutdown() {
        FLUSH_EXECUTOR.shutdown();
        try {
            FLUSH_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        flush();
    }

}
