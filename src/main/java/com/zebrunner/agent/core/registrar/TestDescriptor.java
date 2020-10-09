package com.zebrunner.agent.core.registrar;

class TestDescriptor {

    private final Long zebrunnerId;
    private final TestStartDescriptor startDescriptor;
    private TestFinishDescriptor finishDescriptor;

    private TestDescriptor(Long zebrunnerId, TestStartDescriptor startDescriptor) {
        this.zebrunnerId = zebrunnerId;
        this.startDescriptor = startDescriptor;
    }

    static TestDescriptor create(Long zebrunnerId, TestStartDescriptor startDescriptor) {
        return new TestDescriptor(zebrunnerId, startDescriptor);
    }

    void complete(TestFinishDescriptor finishDescriptor) {
        this.finishDescriptor = finishDescriptor;
    }

    TestStartDescriptor getStartDescriptor() {
        return startDescriptor;
    }

    Long getZebrunnerId() {
        return zebrunnerId;
    }

}
