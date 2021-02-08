package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TestRunDescriptor {

    private final Long zebrunnerId;
    private final TestRunStartDescriptor startDescriptor;
    private TestRunFinishDescriptor finishDescriptor;

    public static TestRunDescriptor create(Long zebrunnerId, TestRunStartDescriptor startDescriptor) {
        return new TestRunDescriptor(zebrunnerId, startDescriptor);
    }

    public void complete(TestRunFinishDescriptor finishDescriptor) {
        this.finishDescriptor = finishDescriptor;
    }

    boolean hasFinished() {
        return finishDescriptor != null;
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
