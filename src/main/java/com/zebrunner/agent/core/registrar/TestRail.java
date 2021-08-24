package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

    public static final String CASE_ID = "com.zebrunner.app/tcm.testrail.case-id";

    public static void disableSync() {
        verifyRunContext();
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        verifyRunContext();
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
    }

    public static void includeAllTestCasesInNewRun() {
        verifyRunContext();
        Label.attachToTestRun(INCLUDE_ALL, "true");
    }

    public static void setSuiteId(String suiteId) {
        verifyRunContext();
        Label.attachToTestRun(SUITE_ID, suiteId);
    }

    public static void setRunId(String runId) {
        verifyRunContext();
        Label.attachToTestRun(RUN_ID, runId);
    }

    public static void setRunName(String runName) {
        verifyRunContext();
        Label.attachToTestRun(RUN_NAME, runName);
    }

    public static void setMilestone(String milestone) {
        verifyRunContext();
        Label.attachToTestRun(MILESTONE, milestone);
    }

    public static void setAssignee(String assignee) {
        verifyRunContext();
        Label.attachToTestRun(ASSIGNEE, assignee);
    }

    public static void setCaseId(String testCaseId) {
        Label.attachToTest(CASE_ID, testCaseId);
    }

    private static void verifyRunContext() {
        if (RunContext.getTests().size() > 0) {
            throw new TestAgentException("TestRail test run labels can't be modified after the start of tests");
        }
    }

}
