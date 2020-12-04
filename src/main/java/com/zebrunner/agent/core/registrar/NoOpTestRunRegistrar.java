package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpTestRunRegistrar implements TestRunRegistrar {

    private static final NoOpTestRunRegistrar INSTANCE = new NoOpTestRunRegistrar();

    public static NoOpTestRunRegistrar getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerStart(TestRunStartDescriptor testRunStartDescriptor) {
        log.trace("Test run started: {}", testRunStartDescriptor);
    }

    @Override
    public void registerFinish(TestRunFinishDescriptor testRunFinishDescriptor) {
        log.trace("Test run finished: {}", testRunFinishDescriptor);
    }

    @Override
    public void registerTestStart(String id, TestStartDescriptor testStartDescriptor) {
        log.trace("Test started: {}", testStartDescriptor);
    }

    @Override
    public void registerHeadlessTestStart(String id, TestStartDescriptor testStartDescriptor) {
        log.trace("Headless test started: {}", testStartDescriptor);
    }

    @Override
    public boolean isTestStarted(String id) {
        log.trace("Is test started: {}", id);
        return true;
    }

    @Override
    public void registerTestFinish(String id, TestFinishDescriptor testFinishDescriptor) {
        log.trace("Test finished: {}", testFinishDescriptor);
    }

}
