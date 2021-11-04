package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestRail {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunRegistrar.class);

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
        verifyTestsStart();
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
        Label.attachToTestRun(INCLUDE_ALL, "true");
        LOGGER.warn("Runtime upload is enabled, all cases will be included in new run by default");
    }

    public static void includeAllTestCasesInNewRun() {
        verifyTestsStart();
        Label.attachToTestRun(INCLUDE_ALL, "true");
    }

    public static void setSuiteId(String suiteId) {
        verifyTestsStart();
        Label.attachToTestRun(SUITE_ID, suiteId);
    }

    public static void setRunId(String runId) {
        verifyTestsStart();
        Label.attachToTestRun(RUN_ID, runId);
    }

    public static void setRunName(String runName) {
        verifyTestsStart();
        Label.attachToTestRun(RUN_NAME, runName);
    }

    public static void setMilestone(String milestone) {
        verifyTestsStart();
        Label.attachToTestRun(MILESTONE, milestone);
    }

    public static void setAssignee(String assignee) {
        verifyTestsStart();
        Label.attachToTestRun(ASSIGNEE, assignee);
    }

    public static void setCaseId(String testCaseId) {
        Label.attachToTest(CASE_ID, testCaseId);
    }

    private static void verifyTestsStart() {
        if (RunContext.hasTests()) {
            throw new TestAgentException("The TestRail configuration must be provided before start of tests. Hint: move the configuration to the code block which is executed before all tests.");
        }
    }

}
