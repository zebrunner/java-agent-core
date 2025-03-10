package com.zebrunner.agent.core.registrar;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;

@UtilityClass
public class Label {

    public static final String LOCALE = "com.zebrunner.app/sut.locale";

    private static final ZebrunnerApiClient API_CLIENT = ApiClientRegistry.getClient();

    public static void attachToTestRun(String name, String... values) {
        Set<com.zebrunner.agent.core.registrar.domain.Label> labels = Label.validateAndConvert(name, values);

        ReportingContext.getTestRun()
                        .ifPresent(testRun -> {
                            API_CLIENT.attachLabelsToTestRun(testRun.getId(), labels);
                            testRun.addLabels(labels);
                        });
    }

    public static void attachToTest(String name, String... values) {
        Set<com.zebrunner.agent.core.registrar.domain.Label> labels = Label.validateAndConvert(name, values);

        ReportingContext.getTestRunId()
                        .ifPresent(testRunId ->
                                ReportingContext.getCurrentTest()
                                                .ifPresent(test -> {
                                                    API_CLIENT.attachLabelsToTest(testRunId, test.getId(), labels);
                                                    test.addLabels(labels);
                                                })
                        );
    }

    private static Set<com.zebrunner.agent.core.registrar.domain.Label> validateAndConvert(String name, String[] values) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name is not provided.");
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Label values are not provided.");
        }

        Set<com.zebrunner.agent.core.registrar.domain.Label> labels = Arrays.stream(values)
                                                                            .filter(Objects::nonNull)
                                                                            .map(value -> new com.zebrunner.agent.core.registrar.domain.Label(name, value))
                                                                            .collect(Collectors.toSet());

        if (labels.isEmpty()) {
            throw new IllegalArgumentException("Label values are not provided.");
        }

        return labels;
    }

}
