package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.registrar.domain.TcmType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zephyr {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zephyr.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zephyr.sync.real-time";

    public static final String TEST_CYCLE_KEY = "com.zebrunner.app/tcm.zephyr.test-cycle-key";
    public static final String JIRA_PROJECT_KEY = "com.zebrunner.app/tcm.zephyr.jira-project-key";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

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

    private static void verifyTestsStart() {
        if (RunContext.hasTests()) {
            throw new TestAgentException("The Zephyr configuration must be provided before start of tests. Hint: move the configuration to the code block which is executed before all tests.");
        }
    }

    public static void setTestCaseKey(String testCaseKey) {
        TEST_CASES_REGISTRY.addTestCasesToCurrentTest(TcmType.ZEPHYR, Collections.singleton(testCaseKey));
    }

    /**
     * Sets the given status for the given test in Zephyr cycle.
     *
     * @param testCaseKey  key of the Zephyr test case
     * @param resultStatus name of the status to be set for the test case
     * @see Scale.SystemTestCaseStatus.Cloud
     * @see Squad.SystemTestCaseStatus.Cloud
     */
    public static void setTestCaseStatus(String testCaseKey, String resultStatus) {
        TEST_CASES_REGISTRY.setCurrentTestTestCaseStatus(TcmType.ZEPHYR, testCaseKey, resultStatus);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Scale {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class SystemTestCaseStatus {

            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static final class Cloud {

                public static final String IN_PROGRESS = "IN PROGRESS";
                public static final String PASS = "PASS";
                public static final String FAIL = "FAIL";
                public static final String NOT_EXECUTED = "NOT EXECUTED";
                public static final String BLOCKED = "BLOCKED";

            }

        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Squad {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class SystemTestCaseStatus {

            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static final class Cloud {

                public static final String UNEXECUTED = "UNEXECUTED";
                public static final String PASS = "PASS";
                public static final String FAIL = "FAIL";
                public static final String WIP = "WIP";
                public static final String BLOCKED = "BLOCKED";

            }

        }

    }

}
