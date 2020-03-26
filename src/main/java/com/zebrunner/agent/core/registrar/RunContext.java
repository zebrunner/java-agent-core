package com.zebrunner.agent.core.registrar;

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

    static void putRun(TestRunDescriptor testRunDescriptor) {
        testRun = testRunDescriptor;
    }

    static TestRunDescriptor getRun() {
        return testRun;
    }

    static void putTest(String uniqueId, TestDescriptor testDescriptor) {
        tests.put(uniqueId, testDescriptor);
    }

    static TestDescriptor getTest(String uniqueId) {
        return tests.get(uniqueId);
    }

}
