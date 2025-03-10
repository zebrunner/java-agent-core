package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.zebrunner.agent.core.registrar.descriptor.TestFinish;
import com.zebrunner.agent.core.registrar.domain.Test;
import com.zebrunner.agent.core.registrar.domain.TestRun;

/**
 * Thread-safe context holder. It is used to keep track between atomic independent test run events
 * occurring in scope of test run: e.g. to match event describing test finish with event describing test start and
 * keep track on test run itself.
 */
@UtilityClass
class ReportingContext {

    @Setter(AccessLevel.PACKAGE)
    private static TestRun testRun;

    private static final Map<String, Test> TESTS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Test> CURRENT_THREAD_LOCAL_TEST = new InheritableThreadLocal<>();
    private static final ThreadLocal<Test> CURRENT_THREAD_LOCAL_AFTER_METHOD = new InheritableThreadLocal<>();
    private static final ThreadLocal<Test> PREVIOUS_COMPLETED_THREAD_LOCAL_TEST = new ThreadLocal<>();

    static Optional<TestRun> getTestRun() {
        return Optional.ofNullable(testRun);
    }

    static Optional<Long> getTestRunId() {
        return Optional.ofNullable(testRun)
                       .map(TestRun::getId);
    }

    static Long getNullableTestRunId() {
        return Optional.ofNullable(testRun)
                       .map(TestRun::getId)
                       .orElse(null);
    }

    static Test getTest(String id) {
        return TESTS.get(id);
    }

    static void addCurrentTest(String id, Test test) {
        TESTS.put(id, test);
        CURRENT_THREAD_LOCAL_TEST.set(test);
    }

    static Optional<Test> getCurrentTest() {
        return Optional.ofNullable(CURRENT_THREAD_LOCAL_TEST.get());
    }

    static Optional<Test> removeCurrentTest() {
        Optional<Test> maybeCurrentTest = getCurrentTest();
        maybeCurrentTest.ifPresent(currentTest -> {
            TESTS.values().removeIf(test -> test == currentTest);
            CURRENT_THREAD_LOCAL_TEST.remove();
        });

        return maybeCurrentTest;
    }

    static void completeTest(String id, TestFinish testFinish) {
        Test testToComplete = ReportingContext.getTest(id);

        if (testToComplete != null) {
            testToComplete.ended(testFinish);

            Test threadLocalTest = CURRENT_THREAD_LOCAL_TEST.get();
            if (threadLocalTest == testToComplete) {
                CURRENT_THREAD_LOCAL_TEST.remove();
                PREVIOUS_COMPLETED_THREAD_LOCAL_TEST.set(testToComplete);
            }
        }
    }

    static void startAfterMethod() {
        // we need to restore previous completed test as current one only if retry is not in progress
        // when retry is in progress, CURRENT_THREAD_LOCAL_TEST stores non-null value
        if (CURRENT_THREAD_LOCAL_TEST.get() == null) {
            Test previousTest = PREVIOUS_COMPLETED_THREAD_LOCAL_TEST.get();
            if (previousTest != null) {
                PREVIOUS_COMPLETED_THREAD_LOCAL_TEST.remove();
                CURRENT_THREAD_LOCAL_TEST.set(previousTest);
                CURRENT_THREAD_LOCAL_AFTER_METHOD.set(previousTest);
            }
        }
    }

    static void finishAfterMethod() {
        Test testToComplete = CURRENT_THREAD_LOCAL_AFTER_METHOD.get();
        if (testToComplete != null) {
            PREVIOUS_COMPLETED_THREAD_LOCAL_TEST.set(testToComplete);
            CURRENT_THREAD_LOCAL_TEST.remove();
            CURRENT_THREAD_LOCAL_AFTER_METHOD.remove();
        }
    }

}
