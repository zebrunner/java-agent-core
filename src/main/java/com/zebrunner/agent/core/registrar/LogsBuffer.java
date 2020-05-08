package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.config.ConfigurationHolder;

import java.util.function.Function;

public interface LogsBuffer<E> {

    static <E> LogsBuffer<E> create(Function<E, Log> converter) {
        return ConfigurationHolder.isEnabled() ? new FlushingLogsBuffer<>(converter) : new NoOpLogsBuffer<>();
    }

    void put(E event);

}
