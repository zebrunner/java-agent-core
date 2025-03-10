package com.zebrunner.agent.core.registrar;

import java.util.function.Function;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.logging.Log;

public interface LogsBuffer<E> {

    static <E> LogsBuffer<E> create(Function<E, Log> converter) {
        if (ConfigurationHolder.isReportingEnabled()) {
            return new FlushingLogsBuffer<>(converter);
        } else {
            return new NoOpLogsBuffer<>();
        }
    }

    void put(E event);

    void flushQueuedConfigurationLogs();

    /**
     * Clear configuration methods logs queue in current thread.
     * Usage example (TestNG): BeforeClass and AfterClass methods will be called in the same thread,
     * as the first test method in class, and AfterClass logs can appear in the next random test method start,
     * so this method should be called after AfterClass calls.
     */
    void clearQueuedConfigurationLogs();

}
