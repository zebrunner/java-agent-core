package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Xray {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.xray.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.xray.sync.real-time";

    public static final String EXECUTION_KEY = "com.zebrunner.app/tcm.xray.test-execution-key";

    public static final String TEST_KEY = "com.zebrunner.app/tcm.xray.test-key";

    public static void disableSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_ENABLED, "false");
    }

    public static void enableRealTimeSync() {
        verifyTestsStart();
        Label.attachToTestRun(SYNC_REAL_TIME, "true");
    }

    public static void setExecutionKey(String executionKey) {
        verifyTestsStart();
        Label.attachToTestRun(EXECUTION_KEY, executionKey);
    }

    public static void setTestKey(String testKey) {
        Label.attachToTest(TEST_KEY, testKey);
    }

    private static void verifyTestsStart() {
        if (RunContext.hasTests()) {
            throw new TestAgentException("The Xray configuration must be provided before start of tests. Hint: move the configuration to the code block which is executed before all tests.");
        }
    }

}
