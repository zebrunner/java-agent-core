package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.domain.TcmType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestRail {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.testrail.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.testrail.sync.real-time";

    public static final String INCLUDE_ALL = "com.zebrunner.app/tcm.testrail.include-all-cases";

    public static final String SUITE_ID = "com.zebrunner.app/tcm.testrail.suite-id";

    public static final String RUN_ID = "com.zebrunner.app/tcm.testrail.run-id";
    public static final String RUN_NAME = "com.zebrunner.app/tcm.testrail.run-name";
    public static final String MILESTONE = "com.zebrunner.app/tcm.testrail.milestone";
    public static final String ASSIGNEE = "com.zebrunner.app/tcm.testrail.assignee";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

    private static volatile boolean isRealTimeSyncEnabled = false;

    public static void disableSync() {
        attachLabelToTestRun(SYNC_ENABLED, "false");
    }

    public static synchronized void enableRealTimeSync() {
        if (!isRealTimeSyncEnabled) {
            attachLabelToTestRun(SYNC_REAL_TIME, "true");
            attachLabelToTestRun(INCLUDE_ALL, "true");
            isRealTimeSyncEnabled = true;
            log.warn("Runtime upload is enabled, all cases will be included in new run by default");
        } else {
            log.warn("Realtime sync for TestRail already enabled.");
        }
    }

    public static void includeAllTestCasesInNewRun() {
        attachLabelToTestRun(INCLUDE_ALL, "true");
    }

    public static void setSuiteId(String suiteId) {
        attachLabelToTestRun(SUITE_ID, suiteId);
    }

    public static void setRunId(String runId) {
        attachLabelToTestRun(RUN_ID, runId);
    }

    public static void setRunName(String runName) {
        attachLabelToTestRun(RUN_NAME, runName);
    }

    public static void setMilestone(String milestone) {
        attachLabelToTestRun(MILESTONE, milestone);
    }

    public static void setAssignee(String assignee) {
        attachLabelToTestRun(ASSIGNEE, assignee);
    }

    private static void attachLabelToTestRun(String name, String... values) {
        if (isRealTimeSyncEnabled) {
            log.warn("Realtime sync for TestRail has been enabled, so you cannot overwrite TestRail configuration");
        } else {
            Label.attachToTestRun(name, values);
        }
    }

    /**
     * Use {@link #setTestCaseId(String)} method instead of this on.
     * <p>
     * Will be removed in 1.8.0 version of the agent.
     */
    @Deprecated
    public static void setCaseId(String testCaseId) {
        setTestCaseId(testCaseId);
    }

    public static void setTestCaseId(String testCaseId) {
        TEST_CASES_REGISTRY.addTestCasesToCurrentTest(TcmType.TEST_RAIL, Collections.singleton(testCaseId));
    }

    /**
     * Sets the given status for the given test case in TestRail run.
     * <p>
     * If you need to use a custom status, contact your TestRail administrator to get the correct system name for your desired status.
     *
     * @param testCaseId   TestRail id of the test case. Can be either a regular number or a number with the letter 'C' at the begging.
     * @param resultStatus system name (not labels!) of the status to be set for the test case
     * @throws NullPointerException if {@code resultStatus} is null
     * @see SystemTestCaseStatus
     */
    public static void setTestCaseStatus(String testCaseId, String resultStatus) {
        TEST_CASES_REGISTRY.setCurrentTestTestCaseStatus(TcmType.TEST_RAIL, testCaseId, resultStatus);
    }

    /**
     * This class contains names (not labels!) of the TestRail system test case result statuses.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SystemTestCaseStatus {

        public static final String PASSED = "passed";
        public static final String BLOCKED = "blocked";
        public static final String RETEST = "retest";
        public static final String FAILED = "failed";

    }

}
