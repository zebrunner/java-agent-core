package com.zebrunner.agent.core.registrar;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

import com.zebrunner.agent.core.registrar.domain.TcmType;

@Slf4j
@UtilityClass
@SuppressWarnings("unused")
public final class TestCase {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zebrunner.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zebrunner.sync.real-time";

    public static final String TEST_RUN_ID = "com.zebrunner.app/tcm.zebrunner.test-run-id";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

    private static volatile boolean isRealTimeSyncEnabled = false;

    @Deprecated
    public static void disableSync() {
        TestCase.attachLabelToTestRun(SYNC_ENABLED, "false");
    }

    @Deprecated
    public static synchronized void enableRealTimeSync() {
        if (!isRealTimeSyncEnabled) {
            TestCase.attachLabelToTestRun(SYNC_REAL_TIME, "true");
            isRealTimeSyncEnabled = true;
        } else {
            log.warn("Realtime sync for Zebrunner TCM already enabled.");
        }
    }

    @Deprecated
    public static void setTestRunId(String id) {
        TestCase.attachLabelToTestRun(TEST_RUN_ID, id);
    }

    @Deprecated
    private static void attachLabelToTestRun(String name, String... values) {
        if (isRealTimeSyncEnabled) {
            log.warn("Realtime sync for Zebrunner TCM has been enabled, so you cannot overwrite TCM configuration");
        } else {
            Label.attachToTestRun(name, values);
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
     * @throws NullPointerException if {@code resultStatus} is null
     * @see SystemTestCaseStatus
     */
    public static void setTestCaseStatus(String testCaseKey, String resultStatus) {
        TEST_CASES_REGISTRY.setCurrentTestTestCaseStatus(TcmType.ZEBRUNNER, testCaseKey, resultStatus);
    }

    @UtilityClass
    public static final class SystemTestCaseStatus {

        public static final String PASSED = "Passed";
        public static final String FAILED = "Failed";
        public static final String SKIPPED = "Skipped";
        public static final String RETEST = "Retest";
        public static final String BLOCKED = "Blocked";
        public static final String INVALID = "Invalid";

    }

}
