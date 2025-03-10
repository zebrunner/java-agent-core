package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.domain.Test;

@Slf4j
public final class Screenshot {

    private static final ZebrunnerApiClient API_CLIENT = ApiClientRegistry.getClient();

    /**
     * Sends screenshot captured in scope of current test execution to Zebrunner. Captured at timestamp accuracy
     * matters - it is strongly recommended to explicitly set this value. If {@code null} is provided - it will be
     * generated automatically
     *
     * @param screenshot       screenshot bytes
     * @param capturedAtMillis unix timestamp representing a moment in time when screenshot got captured in milliseconds
     */
    public static void upload(byte[] screenshot, Long capturedAtMillis) {
        Long capturedAt = capturedAtMillis != null ? capturedAtMillis : System.currentTimeMillis();

        ReportingContext.getTestRunId()
                        .ifPresent(testRunId ->
                                ReportingContext.getCurrentTest()
                                                .map(Test::getId)
                                                .ifPresent(testId ->
                                                        API_CLIENT.uploadScreenshot(screenshot, testRunId, testId, capturedAt)
                                                )
                        );

    }

}
