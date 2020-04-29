package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class ScreenshotUtils {

    private static final ZebrunnerApiClient client = ZebrunnerApiClient.getInstance();

    public static void uploadScreenshot(File screenshot) {
        byte[] screenshotAsByteArray = readBytes(screenshot);

        uploadScreenshot(screenshotAsByteArray);
    }

    public static void uploadScreenshot(byte[] screenshot) {
        String testRunId = RunContext.getRun().getZebrunnerId();
        String testId = RunContext.getCurrentTest().getZebrunnerId();

        client.sendScreenshot(screenshot, testRunId, testId);
    }

    private static byte[] readBytes(File file) {
        byte[] result = null;
        try {
            result = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

}
