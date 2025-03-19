package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.zebrunner.agent.core.registrar.domain.TestFinish;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TestCaseStatusSubmittingRegistrationListener implements RegistrationListener {

    @Getter
    private static final TestCaseStatusSubmittingRegistrationListener instance = new TestCaseStatusSubmittingRegistrationListener();

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onBeforeTestPass(TestFinish testFinish) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestPass();
    }

    @Override
    public void onBeforeTestFail(TestFinish testFinish) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestFail();
    }

    @Override
    public void onBeforeTestSkip(TestFinish testFinish) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestSkip();
    }

    @Override
    public void onBeforeTestBlock(TestFinish testFinish) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestBlock();
    }

}
