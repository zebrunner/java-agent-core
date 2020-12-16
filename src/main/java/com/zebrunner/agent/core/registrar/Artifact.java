package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.ArtifactUploadException;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.domain.ArtifactReferenceDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Artifact {

    private static final ExecutorService UPLOAD_EXECUTOR = Executors.newFixedThreadPool(8);
    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Artifact::shutdown));
    }

    private static void shutdown() {
        UPLOAD_EXECUTOR.shutdown();
        try {
            if (!UPLOAD_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                UPLOAD_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void attachToTestRun(String name, InputStream artifact) {
        Long testRunId = RunContext.getRun().getZebrunnerId();

        UPLOAD_EXECUTOR.execute(() -> API_CLIENT.uploadTestRunArtifact(artifact, name, testRunId));
    }

    public static void attachToTestRun(String name, byte[] artifact) {
        attachToTestRun(name, new ByteArrayInputStream(artifact));
    }

    public static void attachToTestRun(String name, File artifact) {
        try {
            attachToTestRun(name, new FileInputStream(artifact));
        } catch (FileNotFoundException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void attachToTestRun(String name, Path artifact) {
        try {
            attachToTestRun(name, Files.newInputStream(artifact));
        } catch (IOException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void attachReferenceToTestRun(String name, String reference) {
        ArtifactReferenceDTO artifactReference = validateAndConvert(name, reference);
        Long runId = RunContext.getRun().getZebrunnerId();

        API_CLIENT.attachArtifactReferenceToTestRun(runId, artifactReference);
    }

    public static void attachToTest(String name, InputStream artifact) {
        Long testRunId = RunContext.getRun().getZebrunnerId();

        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testId -> UPLOAD_EXECUTOR.execute(
                          () -> API_CLIENT.uploadTestArtifact(artifact, name, testRunId, testId))
                  );
    }

    public static void attachToTest(String name, byte[] artifact) {
        attachToTest(name, new ByteArrayInputStream(artifact));
    }

    public static void attachToTest(String name, File artifact) {
        try {
            attachToTest(name, new FileInputStream(artifact));
        } catch (FileNotFoundException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void attachToTest(String name, Path artifact) {
        try {
            attachToTest(name, Files.newInputStream(artifact));
        } catch (IOException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void attachReferenceToTest(String name, String reference) {
        ArtifactReferenceDTO artifactReference = validateAndConvert(name, reference);
        Long runId = RunContext.getRun().getZebrunnerId();

        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testId -> API_CLIENT.attachArtifactReferenceToTest(runId, testId, artifactReference));
    }

    private static ArtifactReferenceDTO validateAndConvert(String name, String reference) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Artifact reference name is not provided.");
        }
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Artifact reference is not provided.");
        }

        return new ArtifactReferenceDTO(name, reference);
    }

}
