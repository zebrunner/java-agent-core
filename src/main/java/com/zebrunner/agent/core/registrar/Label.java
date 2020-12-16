package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Label {

    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    public static void attachToTestRun(String name, String... values) {
        Set<LabelDTO> labels = validateAndConvert(name, values);
        Long runId = RunContext.getRun().getZebrunnerId();

        API_CLIENT.attachLabelsToTestRun(runId, labels);
    }

    public static void attachToTest(String name, String... values) {
        Set<LabelDTO> labels = validateAndConvert(name, values);
        Long runId = RunContext.getRun().getZebrunnerId();

        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testId -> API_CLIENT.attachLabelsToTest(runId, testId, labels));
    }

    private static Set<LabelDTO> validateAndConvert(String name, String[] values) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name is not provided.");
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Label values are not provided.");
        }

        Set<LabelDTO> labels = Arrays.stream(values)
                                     .filter(Objects::nonNull)
                                     .map(value -> new LabelDTO(name, value))
                                     .collect(Collectors.toSet());

        if (labels.isEmpty()) {
            throw new IllegalArgumentException("Label values are not provided.");
        }

        return labels;
    }

}
