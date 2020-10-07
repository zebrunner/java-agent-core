package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.ArtifactUploadException;
import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class Artifact {

    private static final ExecutorService UPLOAD_EXECUTOR;
    private static final ZebrunnerApiClient API_CLIENT;

    static {
        UPLOAD_EXECUTOR = Executors.newFixedThreadPool(10);
        API_CLIENT = ConfigurationHolder.isReportingEnabled()
                ? ZebrunnerApiClient.getInstance()
                : null;

        Runtime.getRuntime().addShutdownHook(new Thread(Artifact::shutdown));
    }

    public static void upload(InputStream artifact, String name) {
        if (ConfigurationHolder.isReportingEnabled()) {
            String testRunId = String.valueOf(RunContext.getRun().getZebrunnerId());
            String testId = String.valueOf(RunContext.getCurrentTest().getZebrunnerId());

            UPLOAD_EXECUTOR.execute(() -> API_CLIENT.uploadArtifact(artifact, name, testRunId, testId));
        } else {
            log.trace("Artifact is taken: name={}", name);
        }
    }

    public static void upload(byte[] artifact, String name) {
        upload(new ByteArrayInputStream(artifact), name);
    }

    public static void upload(File artifact, String name) {
        try {
            upload(new FileInputStream(artifact), name);
        } catch (FileNotFoundException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void upload(Path artifact, String name) {
        try {
            upload(Files.newInputStream(artifact), name);
        } catch (IOException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    private static void shutdown() {
        UPLOAD_EXECUTOR.shutdown();
        try {
            UPLOAD_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

}
