package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.registrar.domain.TcmType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestCase {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zebrunner.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zebrunner.sync.real-time";

    public static final String TEST_RUN_ID = "com.zebrunner.app/tcm.zebrunner.test-run-id";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

    public static void disableSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
    }

    public static void setTestRunId(String id) {
        verifyTestsStart();
        Label.attachToTestRun(TEST_RUN_ID, id);
    }

    private static void verifyTestsStart() {
        if (RunContext.hasTests()) {
            throw new TestAgentException("This configuration must be provided before start of tests. Hint: move the configuration to the code block which is executed before all tests.");
        }
    }

    public static void setTestCaseKey(String testCaseKey) {
        TEST_CASES_REGISTRY.addTestCasesToCurrentTest(TcmType.ZEBRUNNER, Collections.singleton(testCaseKey));
    }

    /**
     * Sets the given status for the given test case.
     *
     * @param testCaseKey  key of the test case
     * @param resultStatus name of the status to be set for the test case
     * @see SystemTestCaseStatus
     * @see SystemTestCaseStatus
     */
    public static void setTestCaseStatus(String testCaseKey, String resultStatus) {
        TEST_CASES_REGISTRY.setCurrentTestTestCaseStatus(TcmType.ZEBRUNNER, testCaseKey, resultStatus);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SystemTestCaseStatus {

        public static final String PASSED = "Passed";
        public static final String FAILED = "Failed";
        public static final String SKIPPED = "Skipped";
        public static final String RETEST = "Retest";
        public static final String BLOCKED = "Blocked";
        public static final String INVALID = "Invalid";

    }

}
