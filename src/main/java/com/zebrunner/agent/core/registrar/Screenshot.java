package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.rest.ZebrunnerApiClient;

import java.time.Instant;

public final class Screenshot {

    private static final ZebrunnerApiClient client = ZebrunnerApiClient.getInstance();

    /**
     * Sends screenshot captured in scope of current test execution to Zebrunner. Captured at timestamp accuracy
     * matters - it is strongly recommended to explicitly set this value. If {@code null} is provided - it will be
     * generated automatically
     *
     * @param screenshot screenshot bytes
     * @param capturedAt unix timestamp representing a moment in time when screenshot got captured in milliseconds
     */
    public static void upload(byte[] screenshot, Long capturedAt) {
        capturedAt = capturedAt != null ? capturedAt : Instant.now().toEpochMilli();

        String testRunId = RunContext.getRun().getZebrunnerId();
        String testId = RunContext.getCurrentTest().getZebrunnerId();

        client.sendScreenshot(screenshot, testRunId, testId, capturedAt);
    }

}
