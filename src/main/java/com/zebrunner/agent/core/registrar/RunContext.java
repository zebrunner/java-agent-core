package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunDescriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe test run context holder. It is used to keep track between atomic independent test run events
 * occurring in scope of test run: e.g. to match event describing test finish with event describing test start and
 * keep track on test run itself.
 */
class RunContext {

    private static TestRunDescriptor testRun;
    private static final Map<String, TestDescriptor> tests = new ConcurrentHashMap<>();

    /**
     * Accessible just in test lifecycle time
     */
    private static final ThreadLocal<TestDescriptor> threadTest = new ThreadLocal<>();

    static void setRun(TestRunDescriptor testRunDescriptor) {
        RunContext.testRun = testRunDescriptor;
    }

    static TestRunDescriptor getRun() {
        return testRun;
    }

    static void addTest(String id, TestDescriptor testDescriptor) {
        tests.put(id, testDescriptor);
        threadTest.set(testDescriptor);
    }

    static TestDescriptor getTest(String id) {
        return tests.get(id);
    }

    static TestDescriptor getCurrentTest() {
        return threadTest.get();
    }

    static void completeTest(String id, TestFinishDescriptor tf) {
        getTest(id).complete(tf);
        threadTest.remove();
    }

}
