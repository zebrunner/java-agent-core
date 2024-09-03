package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class NoOpLogsBuffer<E> implements LogsBuffer<E> {

    @Override
    public void put(E event) {
        log.trace("Event put to buffer: {}", event);
    }

    @Override
    public void flushQueuedConfigurationLogs() {
        log.trace("Flush queued configuration logs");
    }

    @Override
    public void clearQueuedConfigurationLogs() {
        log.trace("Clear queued configuration logs");
    }

}
