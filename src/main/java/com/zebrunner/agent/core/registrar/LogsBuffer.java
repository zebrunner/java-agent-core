package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public interface LogsBuffer<E> {

    Logger LOGGER = LoggerFactory.getLogger(TestRunRegistrar.class);

    static <E> LogsBuffer<E> create(Function<E, Log> converter) {
        if (ConfigurationHolder.isEnabled()) {
            return new FlushingLogsBuffer<>(converter);
        } else {
            LOGGER.warn("Reporting disabled: using no op logs buffer");
            return new NoOpLogsBuffer<>();
        }
    }

    void put(E event);

}