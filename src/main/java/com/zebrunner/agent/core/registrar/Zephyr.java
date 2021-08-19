package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zephyr {

    private static final String SYNC_ENABLED = "com.zebrunner.app/tcm.zephyr.sync.enabled";
    private static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.zephyr.sync.real-time";

    private static final String TEST_CYCLE_KEY = "com.zebrunner.app/tcm.zephyr.test-cycle-key";
    private static final String TEST_CASE_KEY = "com.zebrunner.app/tcm.zephyr.test-case-key";

    public static void disableSync() {
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
    }

    public static void setTestCycleKey(String testCycleKey) {
        Label.attachToTestRun(TEST_CYCLE_KEY, testCycleKey);
    }

    public static void setTestCaseKey(String testCaseKey) {
        Label.attachToTest(TEST_CASE_KEY, testCaseKey);
    }

}
