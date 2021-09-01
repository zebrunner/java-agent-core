package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zephyr {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zephyr.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zephyr.sync.real-time";

    public static final String TEST_CYCLE_KEY = "com.zebrunner.app/tcm.zephyr.test-cycle-key";
    public static final String JIRA_PROJECT_KEY = "com.zebrunner.app/tcm.zephyr.jira-project-key";
    public static final String TEST_CASE_KEY = "com.zebrunner.app/tcm.zephyr.test-case-key";

    public static void disableSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
    }

    public static void setTestCycleKey(String testCycleKey) {
        verifyTestsStart();
        Label.attachToTestRun(TEST_CYCLE_KEY, testCycleKey);
    }

    public static void setJiraProjectKey(String jiraProjectKey) {
        verifyTestsStart();
        Label.attachToTestRun(JIRA_PROJECT_KEY, jiraProjectKey);
    }

    public static void setTestCaseKey(String testCaseKey) {
        Label.attachToTest(TEST_CASE_KEY, testCaseKey);
    }

    private static void verifyTestsStart() {
        if (RunContext.getTests().size() > 0) {
            throw new TestAgentException("Zephyr test run labels can't be modified after the start of tests");
        }
    }

}
