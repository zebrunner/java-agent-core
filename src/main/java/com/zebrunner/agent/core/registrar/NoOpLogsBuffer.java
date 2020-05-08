package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class NoOpLogsBuffer<E> implements LogsBuffer<E> {

    private static final String MSG_NO_OP_COMPONENT_SWITCH = "Reporting disabled: switching to no op components";

    @Override
    public void put(E event) {
        log.trace(MSG_NO_OP_COMPONENT_SWITCH);
    }
}
