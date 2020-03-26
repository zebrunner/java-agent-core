package com.zebrunner.agent.core.registrar;

class TestDescriptor {

    private final String zebrunnerId;
    private final TestStartDescriptor startDescriptor;
    private TestFinishDescriptor finishDescriptor;

    private TestDescriptor(String zebrunnerId, TestStartDescriptor startDescriptor) {
        this.zebrunnerId = zebrunnerId;
        this.startDescriptor = startDescriptor;
    }

    static TestDescriptor create(String zebrunnerId, TestStartDescriptor startDescriptor) {
        return new TestDescriptor(zebrunnerId, startDescriptor);
    }

    void complete(TestFinishDescriptor finishDescriptor) {
        this.finishDescriptor = finishDescriptor;
    }

    String getZebrunnerId() {
        return zebrunnerId;
    }

}
