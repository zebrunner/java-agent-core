package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public final class Screenshot {

    private static final ZebrunnerApiClient API_CLIENT = ConfigurationHolder.isEnabled()
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
        if (ConfigurationHolder.isEnabled()) {
            capturedAtMillis = capturedAtMillis != null ? capturedAtMillis : Instant.now().toEpochMilli();

            String testRunId = String.valueOf(RunContext.getRun().getZebrunnerId());
            String testId = String.valueOf(RunContext.getCurrentTest().getZebrunnerId());

            API_CLIENT.sendScreenshot(screenshot, testRunId, testId, capturedAtMillis);
        } else {
            log.trace("Screenshot taken: size={}, captureAtMillis={}", screenshot.length, capturedAtMillis);
        }
    }

}
