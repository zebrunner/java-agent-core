package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Screenshot {

    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    /**
     * Sends screenshot captured in scope of current test execution to Zebrunner. Captured at timestamp accuracy
     * matters - it is strongly recommended to explicitly set this value. If {@code null} is provided - it will be
     * generated automatically
     *
     * @param screenshot       screenshot bytes
     * @param capturedAtMillis unix timestamp representing a moment in time when screenshot got captured in milliseconds
     */
    public static void upload(byte[] screenshot, Long capturedAtMillis) {
        capturedAtMillis = capturedAtMillis != null ? capturedAtMillis : System.currentTimeMillis();

        Long testRunId = RunContext.getRun().getZebrunnerId();
        Long testId = RunContext.getCurrentTest().getZebrunnerId();

        API_CLIENT.uploadScreenshot(screenshot, testRunId, testId, capturedAtMillis);
    }

}
