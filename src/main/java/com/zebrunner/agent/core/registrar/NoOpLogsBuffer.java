package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class NoOpLogsBuffer<E> implements LogsBuffer<E> {

    @Override
    public void put(E event) {
        log.trace("Event put to buffer: {}", event);
    }

}
