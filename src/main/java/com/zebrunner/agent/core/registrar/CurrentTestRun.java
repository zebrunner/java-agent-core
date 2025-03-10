package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;

public class CurrentTestRun {

    private static final ZebrunnerApiClient API_CLIENT = ApiClientRegistry.getClient();

    public static void setBuild(String build) {
        if (build == null || build.trim().isEmpty()) {
            throw new TestAgentException("Test Run build must not be empty.");
        }

        ReportingContext.getTestRun()
                        .ifPresent(testRun -> {
                            API_CLIENT.patchTestRunBuild(testRun.getId(), build);
                            testRun.setBuild(build);
                        });
    }

    public static void setLocale(String locale) {
        if (locale == null || locale.trim().isEmpty()) {
            throw new TestAgentException("Test Run locale must not be empty.");
        }

        ReportingContext.getTestRun()
                        .ifPresent(testRun -> {
                            Label.attachToTestRun(Label.LOCALE, locale);
                            testRun.setLocale(locale);
                        });
    }

    public static void setPlatform(String name) {
        CurrentTestRun.setPlatform(name, null);
    }

    public static void setPlatform(String name, String version) {
        if (name == null || name.trim().isEmpty()) {
            throw new TestAgentException("Test Run platform name must not be empty.");
        }

        ReportingContext.getTestRunId()
                        .ifPresent(testRunId -> API_CLIENT.patchTestRunPlatform(testRunId, name, version));
    }

}
