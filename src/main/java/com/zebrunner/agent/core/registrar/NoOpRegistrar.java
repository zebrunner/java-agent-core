package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpRegistrar implements TestRunRegistrar {

    private static final String MSG_NO_OP_COMPONENT_SWITCH = "Reporting disabled: switching to no op components";

    private static final NoOpRegistrar INSTANCE = new NoOpRegistrar();

    @Override
    public void start(TestRunStartDescriptor testRunStartDescriptor) {
        log.trace(MSG_NO_OP_COMPONENT_SWITCH);
    }

    @Override
    public void finish(TestRunFinishDescriptor testRunFinishDescriptor) {
        log.trace(MSG_NO_OP_COMPONENT_SWITCH);
    }

    @Override
    public void startTest(String uniqueId, TestStartDescriptor testStartDescriptor) {
        log.trace(MSG_NO_OP_COMPONENT_SWITCH);
    }

    @Override
    public void finishTest(String uniqueId, TestFinishDescriptor testFinishDescriptor) {
        log.trace(MSG_NO_OP_COMPONENT_SWITCH);
    }

    public static NoOpRegistrar getInstance() {
        return INSTANCE;
    }
}
