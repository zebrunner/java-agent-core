package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.TestLabel;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestLabelResolver implements LabelResolver {

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        Map<String, List<String>> labels = getAnnotations(clazz);
        labels.putAll(getAnnotations(method));

        return labels;
    }

    private Map<String, List<String>> getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(TestLabel.List.class))
                       .map(TestLabel.List::value)
                       .map(Arrays::stream)
                       .orElseGet(() -> Stream.of(annotatedElement.getAnnotation(TestLabel.class)))
                       .filter(Objects::nonNull)
                       .collect(Collectors.toMap(
                               TestLabel::name,
                               testLabel -> new ArrayList<>(Arrays.asList(testLabel.value())),
                               this::union
                       ));
    }

    private List<String> union(List<String> values1, List<String> values2) {
        ArrayList<String> values = new ArrayList<>(values1);
        values.addAll(values2);
        return values;
    }

}
