package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.domain.TcmType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zephyr {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zephyr.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zephyr.sync.real-time";

    public static final String TEST_CYCLE_KEY = "com.zebrunner.app/tcm.zephyr.test-cycle-key";
    public static final String JIRA_PROJECT_KEY = "com.zebrunner.app/tcm.zephyr.jira-project-key";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

    private static volatile boolean isRealTimeSyncEnabled = false;

    public static void disableSync() {
        attachLabelToTestRun(SYNC_ENABLED, "false");
    }

    public static synchronized void enableRealTimeSync() {
        if (!isRealTimeSyncEnabled) {
            attachLabelToTestRun(SYNC_REAL_TIME, "true");
            isRealTimeSyncEnabled = true;
        } else {
            log.warn("Realtime sync for Zephyr already enabled.");
        }
    }

    public static void setTestCycleKey(String testCycleKey) {
        attachLabelToTestRun(TEST_CYCLE_KEY, testCycleKey);
    }

    public static void setJiraProjectKey(String jiraProjectKey) {
        attachLabelToTestRun(JIRA_PROJECT_KEY, jiraProjectKey);
    }

    private static void attachLabelToTestRun(String name, String... values) {
        if (isRealTimeSyncEnabled) {
            log.warn("Realtime sync for Zephyr has been enabled, so you cannot overwrite Zephyr configuration");
        } else {
            Label.attachToTestRun(name, values);
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
