package com.zebrunner.agent.core.registrar;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

import com.zebrunner.agent.core.registrar.domain.TcmType;

@Slf4j
@UtilityClass
@SuppressWarnings("unused")
public final class Xray {

    public static final String SYNC_ENABLED = "com.zebrunner.app/tcm.xray.sync.enabled";
    public static final String SYNC_REAL_TIME = "com.zebrunner.app/tcm.xray.sync.real-time";

    public static final String EXECUTION_KEY = "com.zebrunner.app/tcm.xray.test-execution-key";

    private static final TestCasesRegistry TEST_CASES_REGISTRY = TestCasesRegistry.getInstance();

    private static volatile boolean isRealTimeSyncEnabled = false;

    public static void disableSync() {
        Xray.attachLabelToTestRun(SYNC_ENABLED, "false");
    }

    public static synchronized void enableRealTimeSync() {
        if (!isRealTimeSyncEnabled) {
            Xray.attachLabelToTestRun(SYNC_REAL_TIME, "true");
            isRealTimeSyncEnabled = true;
        } else {
            log.warn("Realtime sync for Xray already enabled");
        }
    }

    public static void setExecutionKey(String executionKey) {
        Xray.attachLabelToTestRun(EXECUTION_KEY, executionKey);
    }

    private static void attachLabelToTestRun(String name, String... values) {
        if (isRealTimeSyncEnabled) {
            log.warn("Realtime sync for Xray has been enabled, so you cannot overwrite Xray configuration");
        } else {
            Label.attachToTestRun(name, values);
        }
    }

    public static void setTestKey(String testKey) {
        TEST_CASES_REGISTRY.addTestCasesToCurrentTest(TcmType.XRAY, Collections.singleton(testKey));
    }

    /**
     * Sets the given status for the given test in Xray execution.
     *
     * @param testKey      key of the Xray test
     * @param resultStatus name of the status to be set for the test
     * @throws NullPointerException if {@code resultStatus} is null
     * @see SystemTestStatus.Cloud
     * @see SystemTestStatus.Server
     */
    public static void setTestStatus(String testKey, String resultStatus) {
        TEST_CASES_REGISTRY.setCurrentTestTestCaseStatus(TcmType.XRAY, testKey, resultStatus);
    }

    @UtilityClass
    public static final class SystemTestStatus {

        @UtilityClass
        public static final class Cloud {

            public static final String PASSED = "PASSED";
            public static final String EXECUTING = "EXECUTING";
            public static final String FAILED = "FAILED";
            public static final String NOT_EXECUTED = "NOT_EXECUTED";

        }

        @UtilityClass
        public static final class Server {

            public static final String PASS = "PASS";
            public static final String FAIL = "FAIL";

        }

    }

}
