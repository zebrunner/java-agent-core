package com.zebrunner.agent.core.registrar;

class StdoutRegistrar implements TestRunRegistrar {

    private static final StdoutRegistrar INSTANCE = new StdoutRegistrar();

    @Override
    public void start(TestRunStartDescriptor testRunStartDescriptor) {
        System.out.println("Test run started: " + testRunStartDescriptor.getName());
    }

    @Override
    public void finish(TestRunFinishDescriptor testRunFinishDescriptor) {
        System.out.println("Test run finished: " + testRunFinishDescriptor);
    }

    @Override
    public void startTest(String uniqueId, TestStartDescriptor testStartDescriptor) {
        System.out.println("Test [" + testStartDescriptor + "] started");
    }

    @Override
    public void finishTest(String uniqueId, TestFinishDescriptor testFinishDescriptor) {
        System.out.println("Test [" + testFinishDescriptor + "] finished: " + testFinishDescriptor.getStatus());
    }

    public static StdoutRegistrar getInstance() {
        return INSTANCE;
    }

}
