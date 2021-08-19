package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Xray {

    private static final String SYNC_ENABLED = "com.zebrunner.app/tcm.xray.sync.enabled";
    private static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.xray.sync.real-time";

    private static final String EXECUTION_KEY = "com.zebrunner.app/tcm.xray.test-execution-key";
    private static final String TEST_KEY = "com.zebrunner.app/tcm.xray.test-key";

    public static void disableSync() {
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
    }

    public static void setExecutionKey(String executionKey) {
        Label.attachToTestRun(EXECUTION_KEY, executionKey);
    }

    public static void setTestKey(String testKey) {
        Label.attachToTest(TEST_KEY, testKey);
    }

}
