package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.client.ZebrunnerApiClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public final class Screenshot {

    private static final ZebrunnerApiClient API_CLIENT = ConfigurationHolder.isReportingEnabled()
            ? ZebrunnerApiClient.getInstance()
            : null;

    /**
     * Sends screenshot captured in scope of current test execution to Zebrunner. Captured at timestamp accuracy
     * matters - it is strongly recommended to explicitly set this value. If {@code null} is provided - it will be
     * generated automatically
     *
     * @param screenshot       screenshot bytes
     * @param capturedAtMillis unix timestamp representing a moment in time when screenshot got captured in milliseconds
     */
    public static void upload(byte[] screenshot, Long capturedAtMillis) {
        if (ConfigurationHolder.isReportingEnabled()) {
            capturedAtMillis = capturedAtMillis != null ? capturedAtMillis : Instant.now().toEpochMilli();

            Long testRunId = RunContext.getRun().getZebrunnerId();
            Long testId = RunContext.getCurrentTest().getZebrunnerId();

            API_CLIENT.uploadScreenshot(screenshot, testRunId, testId, capturedAtMillis);
        } else {
            log.trace("Screenshot taken: size={}, captureAtMillis={}", screenshot.length, capturedAtMillis);
        }
    }

}
