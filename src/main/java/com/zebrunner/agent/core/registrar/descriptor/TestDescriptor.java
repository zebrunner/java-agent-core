package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDescriptor {

    private final Long zebrunnerId;
    private final TestStartDescriptor startDescriptor;
    private TestFinishDescriptor finishDescriptor;

    public static TestDescriptor create(Long zebrunnerId, TestStartDescriptor startDescriptor) {
        return new TestDescriptor(zebrunnerId, startDescriptor);
    }

    public void complete(TestFinishDescriptor finishDescriptor) {
        this.finishDescriptor = finishDescriptor;
    }

}
