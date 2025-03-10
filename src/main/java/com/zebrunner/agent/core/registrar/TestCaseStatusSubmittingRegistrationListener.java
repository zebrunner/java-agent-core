package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;

class TestCaseStatusSubmittingRegistrationListener implements RegistrationListener {

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onBeforeTestPass(TestFinishDescriptor finishDescriptor) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestPass();
    }

    @Override
    public void onBeforeTestFail(TestFinishDescriptor finishDescriptor) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestFail();
    }

    @Override
    public void onBeforeTestSkip(TestFinishDescriptor finishDescriptor) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestSkip();
    }

    @Override
    public void onBeforeTestBlock(TestFinishDescriptor finishDescriptor) {
        testCasesRegistry.setExplicitStatusesOnCurrentTestBlock();
    }

}
