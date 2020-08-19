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

@Slf4j
public final class Artifact {

    private static final ZebrunnerApiClient API_CLIENT = ConfigurationHolder.isReportingEnabled()
            ? ZebrunnerApiClient.getInstance()
            : null;

    public static void upload(InputStream artifact, String name) {
        if (ConfigurationHolder.isReportingEnabled()) {
            String testRunId = String.valueOf(RunContext.getRun().getZebrunnerId());
            String testId = String.valueOf(RunContext.getCurrentTest().getZebrunnerId());

            API_CLIENT.uploadArtifact(artifact, name, testRunId, testId);
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

}
