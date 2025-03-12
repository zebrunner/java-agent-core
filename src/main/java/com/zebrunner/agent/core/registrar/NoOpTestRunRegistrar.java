package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.zebrunner.agent.core.registrar.domain.TestFinish;
import com.zebrunner.agent.core.registrar.domain.TestRunFinish;
import com.zebrunner.agent.core.registrar.domain.TestRunStart;
import com.zebrunner.agent.core.registrar.domain.TestStart;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NoOpTestRunRegistrar implements TestRunRegistrar {

    @Getter
    private static final NoOpTestRunRegistrar instance = new NoOpTestRunRegistrar();

    @Override
    public void registerStart(TestRunStart testRunStart) {
        log.trace("Test run started: {}", testRunStart);
    }

    @Override
    public void registerFinish(TestRunFinish testRunFinish) {
        log.trace("Test run finished: {}", testRunFinish);
    }

    @Override
    public void registerTestStart(String id, TestStart testStart) {
        log.trace("Test started: {}", testStart);
    }

    @Override
    public boolean isTestStarted() {
        log.trace("Is test started");
        return true;
    }

    @Override
    public void registerHeadlessTestStart(String id, TestStart testStart) {
        log.trace("Headless test started: {}", testStart);
    }

    @Override
    public boolean isTestStarted(String id) {
        log.trace("Is test started: {}", id);
        return true;
    }

    @Override
    public void registerTestFinish(String id, TestFinish testFinish) {
        log.trace("Test finished: {}", testFinish);
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

    @Override
    public void clearConfigurationLogs() {
        log.trace("Clear configuration logs");
    }

}
