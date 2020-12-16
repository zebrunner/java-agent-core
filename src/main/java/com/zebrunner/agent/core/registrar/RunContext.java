package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunDescriptor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe test run context holder. It is used to keep track between atomic independent test run events
 * occurring in scope of test run: e.g. to match event describing test finish with event describing test start and
 * keep track on test run itself.
 */
class RunContext {

    private static TestRunDescriptor testRun;
    private static final Map<String, TestDescriptor> TESTS = new ConcurrentHashMap<>();
    private static final ThreadLocal<TestDescriptor> THREAD_LOCAL_TEST = new InheritableThreadLocal<>();

    static void setRun(TestRunDescriptor testRunDescriptor) {
        RunContext.testRun = testRunDescriptor;
    }

    static TestRunDescriptor getRun() {
        return testRun;
    }

    static void addTest(String id, TestDescriptor testDescriptor) {
        TESTS.put(id, testDescriptor);
        THREAD_LOCAL_TEST.set(testDescriptor);
    }

    static TestDescriptor getTest(String id) {
        return TESTS.get(id);
    }

    static Optional<TestDescriptor> getCurrentTest() {
        return Optional.ofNullable(THREAD_LOCAL_TEST.get());
    }

    static Optional<TestDescriptor> removeCurrentTest() {
        Optional<TestDescriptor> maybeCurrentTest = getCurrentTest();
        maybeCurrentTest.ifPresent(currentTest -> {
            TESTS.values().removeIf(test -> test == currentTest);
            THREAD_LOCAL_TEST.remove();
        });

        return maybeCurrentTest;
    }

    static void completeTest(String id, TestFinishDescriptor tf) {
        TestDescriptor testToComplete = getTest(id);

        if (testToComplete != null) {
            testToComplete.complete(tf);

            TestDescriptor threadLocalTest = THREAD_LOCAL_TEST.get();
            if (threadLocalTest == testToComplete) {
                THREAD_LOCAL_TEST.remove();
            }
        }
    }

}
