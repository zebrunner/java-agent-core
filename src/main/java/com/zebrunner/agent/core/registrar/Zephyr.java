package com.zebrunner.agent.core.registrar;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

import com.zebrunner.agent.core.registrar.domain.TcmType;

@Slf4j
@UtilityClass
@SuppressWarnings("unused")
public final class Zephyr {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zephyr.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zephyr.sync.real-time";

    public static final String TEST_CYCLE_KEY = "com.zebrunner.app/tcm.zephyr.test-cycle-key";
    public static final String JIRA_PROJECT_KEY = "com.zebrunner.app/tcm.zephyr.jira-project-key";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

    private static volatile boolean isRealTimeSyncEnabled = false;

    @Deprecated
    public static void disableSync() {
        Zephyr.attachLabelToTestRun(SYNC_ENABLED, "false");
    }

    @Deprecated
    public static synchronized void enableRealTimeSync() {
        if (!isRealTimeSyncEnabled) {
            Zephyr.attachLabelToTestRun(SYNC_REAL_TIME, "true");
            isRealTimeSyncEnabled = true;
        } else {
            log.warn("Realtime sync for Zephyr already enabled.");
        }
    }

    @Deprecated
    public static void setTestCycleKey(String testCycleKey) {
        Zephyr.attachLabelToTestRun(TEST_CYCLE_KEY, testCycleKey);
    }

    @Deprecated
    public static void setJiraProjectKey(String jiraProjectKey) {
        Zephyr.attachLabelToTestRun(JIRA_PROJECT_KEY, jiraProjectKey);
    }

    @Deprecated
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
     * @throws NullPointerException if {@code resultStatus} is null
     * @see Scale.SystemTestCaseStatus.Cloud
     * @see Squad.SystemTestCaseStatus.Cloud
     */
    public static void setTestCaseStatus(String testCaseKey, String resultStatus) {
        TEST_CASES_REGISTRY.setCurrentTestTestCaseStatus(TcmType.ZEPHYR, testCaseKey, resultStatus);
    }

    @UtilityClass
    public static final class Scale {

        @UtilityClass
        public static final class SystemTestCaseStatus {

            @UtilityClass
            public static final class Cloud {

                public static final String IN_PROGRESS = "IN PROGRESS";
                public static final String PASS = "PASS";
                public static final String FAIL = "FAIL";
                public static final String NOT_EXECUTED = "NOT EXECUTED";
                public static final String BLOCKED = "BLOCKED";

            }

        }

    }

    @UtilityClass
    public static final class Squad {

        @UtilityClass
        public static final class SystemTestCaseStatus {

            @UtilityClass
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
