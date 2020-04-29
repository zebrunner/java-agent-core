package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class TestRunDescriptor {

    private final String zebrunnerId;
    private final TestRunStartDescriptor startDescriptor;
    private TestRunFinishDescriptor finishDescriptor;

    static TestRunDescriptor create(String zebrunnerId, TestRunStartDescriptor startDescriptor) {
        return new TestRunDescriptor(zebrunnerId, startDescriptor);
    }

    void complete(TestRunFinishDescriptor finishDescriptor) {
        this.finishDescriptor = finishDescriptor;
    }

    boolean hasFinished() {
        return finishDescriptor != null;
    }

    String getZebrunnerId() {
        return zebrunnerId;
    }

    String getName() {
        return startDescriptor.getName();
    }

    OffsetDateTime getStartedAt() {
        return startDescriptor.getStartedAt();
    }

    OffsetDateTime getEndedAt() {
        return finishDescriptor.getEndedAt();
    }

}
