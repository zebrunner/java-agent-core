package com.zebrunner.agent.core.registrar;

import lombok.experimental.UtilityClass;
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

import com.zebrunner.agent.core.exception.ArtifactUploadException;
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.domain.ArtifactReference;
import com.zebrunner.agent.core.registrar.domain.Test;

@Slf4j
@UtilityClass
@SuppressWarnings("unused")
public final class Artifact {

    private static final ExecutorService UPLOAD_EXECUTOR = Executors.newFixedThreadPool(8);
    private static final ZebrunnerApiClient API_CLIENT = ApiClientRegistry.getClient();

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void attachToTestRun(String name, InputStream artifact) {
        ReportingContext.getTestRunId()
                        .ifPresentOrElse(
                                testRunId -> UPLOAD_EXECUTOR.execute(() -> API_CLIENT.uploadTestRunArtifact(artifact, name, testRunId)),
                                () -> log.error("Failed to attach artifact '{}' to test run because it has not been started yet.", name)
                        );
    }

    public static void attachToTestRun(String name, byte[] artifact) {
        Artifact.attachToTestRun(name, new ByteArrayInputStream(artifact));
    }

    public static void attachToTestRun(String name, File artifact) {
        try {
            Artifact.attachToTestRun(name, new FileInputStream(artifact));
        } catch (FileNotFoundException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void attachToTestRun(String name, Path artifact) {
        try {
            Artifact.attachToTestRun(name, Files.newInputStream(artifact));
        } catch (IOException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void attachToTest(String name, InputStream artifact) {
        ReportingContext.getTestRunId()
                        .ifPresentOrElse(
                                testRunId -> ReportingContext.getCurrentTest()
                                                             .map(Test::getId)
                                                             .ifPresentOrElse(
                                                                     testId -> UPLOAD_EXECUTOR.execute(() -> API_CLIENT.uploadTestArtifact(artifact, name, testRunId, testId)),
                                                                     () -> log.error("Failed to attach artifact '{}' to test because it has not been started yet.", name)
                                                             ),
                                () -> log.error("Failed to attach artifact '{}' to test because test run has not been started yet.", name)
                        );
    }

    public static void attachToTest(String name, byte[] artifact) {
        Artifact.attachToTest(name, new ByteArrayInputStream(artifact));
    }

    public static void attachToTest(String name, File artifact) {
        try {
            Artifact.attachToTest(name, new FileInputStream(artifact));
        } catch (FileNotFoundException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    public static void attachToTest(String name, Path artifact) {
        try {
            Artifact.attachToTest(name, Files.newInputStream(artifact));
        } catch (IOException e) {
            throw new ArtifactUploadException("Unable to upload artifact with name " + name, e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void attachReferenceToTestRun(String name, String reference) {
        ArtifactReference artifactReference = Artifact.validateAndBuildReference(name, reference);

        ReportingContext.getTestRun()
                        .ifPresentOrElse(
                                testRun -> {
                                    API_CLIENT.attachArtifactReferenceToTestRun(testRun.getId(), artifactReference);
                                    testRun.addArtifactReference(artifactReference);
                                },
                                () -> log.error("Failed to attach artifact reference '{}' to test run because it has not been started yet.", name)
                        );
    }

    public static void attachReferenceToTest(String name, String reference) {
        ArtifactReference artifactReference = Artifact.validateAndBuildReference(name, reference);

        ReportingContext.getTestRunId()
                        .ifPresentOrElse(
                                testRunId -> ReportingContext.getCurrentTest()
                                                             .ifPresentOrElse(
                                                                     test -> {
                                                                         UPLOAD_EXECUTOR.execute(() -> API_CLIENT.attachArtifactReferenceToTest(testRunId, test.getId(), artifactReference));
                                                                         test.addArtifactReference(artifactReference);
                                                                     },
                                                                     () -> log.error("Failed to attach artifact reference '{}' to test because it has not been started yet.", name)
                                                             ),
                                () -> log.error("Failed to attach artifact reference '{}' to test because test run has not been started yet.", name)
                        );
    }

    private static ArtifactReference validateAndBuildReference(String name, String reference) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Artifact reference name is not provided.");
        }
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Artifact reference is not provided.");
        }

        return new ArtifactReference(name, reference);
    }

}
