package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class CurrentTestRun {

    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    /**
     * This method returns Zebrunner Test Run id.
     * @return if test run has not been reported yet - empty {@link Optional}, otherwise - {@link Optional} containing Zebrunner Test Run id.
     */
    public static Optional<Long> getId() {
        return Optional.ofNullable(RunContext.getZebrunnerRunId());
    }

    public static void setBuild(String build) {
        if (build == null || build.trim().isEmpty()) {
            throw new TestAgentException("Test Run build must not be empty.");
        }

        Long runId = RunContext.getZebrunnerRunId();
        if (runId != null) {
            API_CLIENT.patchTestRunBuild(runId, build);
        }
    }

    public static void setLocale(String locale) {
        if (locale == null || locale.trim().isEmpty()) {
            throw new TestAgentException("Test Run locale must not be empty.");
        }

        Label.attachToTestRun(Label.LOCALE, locale);
    }

    public static void setPlatform(String name) {
        setPlatform(name, null);
    }

    public static void setPlatform(String name, String version) {
        if (name == null || name.trim().isEmpty()) {
            throw new TestAgentException("Test Run platform name must not be empty.");
        }

        Long runId = RunContext.getZebrunnerRunId();
        if (runId != null) {
            API_CLIENT.setTestRunPlatform(runId, name, version);
        }
    }

}
