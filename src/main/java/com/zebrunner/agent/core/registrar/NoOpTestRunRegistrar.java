package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
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
    public boolean isTestStarted() {
        log.trace("Is test started");
        return true;
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

    @Override
    public void registerAfterTestStart() {
        log.trace("After Test started");
    }

    @Override
    public void registerAfterTestFinish() {
        log.trace("After Test finished");
    }

    @Override
    public boolean isKnownIssueAttachedToTest(String failureStacktrace) {
        log.trace("Is known issue attached to test");
        return false;
    }

}
