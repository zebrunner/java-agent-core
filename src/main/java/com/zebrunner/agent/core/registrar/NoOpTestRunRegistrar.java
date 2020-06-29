package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpTestRunRegistrar implements TestRunRegistrar {

    private static final NoOpTestRunRegistrar INSTANCE = new NoOpTestRunRegistrar();

    @Override
    public void start(TestRunStartDescriptor testRunStartDescriptor) {
        log.trace("Test run started: {}", testRunStartDescriptor);
    }

    @Override
    public void finish(TestRunFinishDescriptor testRunFinishDescriptor) {
        log.trace("Test run finished: {}", testRunFinishDescriptor);
    }

    @Override
    public void startTest(String uniqueId, TestStartDescriptor testStartDescriptor) {
        log.trace("Test started: {}", testStartDescriptor);
    }

    @Override
    public boolean isTestStarted(String uniqueId) {
        log.trace("Is test started: {}", uniqueId);
        return true;
    }

    @Override
    public void finishTest(String uniqueId, TestFinishDescriptor testFinishDescriptor) {
        log.trace("Test finished: {}", testFinishDescriptor);
    }

    public static NoOpTestRunRegistrar getInstance() {
        return INSTANCE;
    }

}
