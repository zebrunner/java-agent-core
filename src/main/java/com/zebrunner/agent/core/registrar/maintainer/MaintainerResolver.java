package com.zebrunner.agent.core.registrar.maintainer;

import java.lang.reflect.Method;
import java.util.List;

import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.domain.TestRun;
import com.zebrunner.agent.core.registrar.domain.TestSession;

public interface MaintainerResolver {

    static MaintainerResolver getInstance() {
        return ChainedMaintainerResolver.getInstance();
    }

    default String resolve(TestRun testRun, TestStart testStart, List<TestSession> testSessions) {
        return this.resolve(testStart.getTestClass(), testStart.getTestMethod());
    }

    @Deprecated
    @SuppressWarnings("unused")
    default String resolve(Class<?> clazz, Method method) {
        return null;
    }

}
