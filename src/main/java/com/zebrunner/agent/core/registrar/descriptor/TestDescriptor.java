package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDescriptor {

    private final Long zebrunnerId;
    private final TestStartDescriptor startDescriptor;
    private TestFinishDescriptor finishDescriptor;

    public static TestDescriptor create(Long zebrunnerId, TestStartDescriptor startDescriptor) {
        return new TestDescriptor(zebrunnerId, startDescriptor);
    }

    public Class<?> getTestClass() {
        if (startDescriptor == null) {
            return null;
        }
        return startDescriptor.getTestClass();
    }

    public Method getTestMethod() {
        if (startDescriptor == null) {
            return null;
        }
        return startDescriptor.getTestMethod();
    }

    public void complete(TestFinishDescriptor finishDescriptor) {
        this.finishDescriptor = finishDescriptor;
    }

}
