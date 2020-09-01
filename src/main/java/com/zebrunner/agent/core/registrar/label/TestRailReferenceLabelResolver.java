package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.TestRailReference;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRailReferenceLabelResolver implements LabelResolver {

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        TestRailReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        List<String> values = Optional.ofNullable(annotations)
                                      .map(Arrays::stream)
                                      .orElseGet(Stream::empty)
                                      .map(TestRailReference::value)
                                      .flatMap(Arrays::stream)
                                      .collect(Collectors.toList());

        return values.isEmpty()
                ? Collections.emptyMap()
                : Collections.singletonMap(Labels.TEST_RAIL_REFERENCE, values);
    }

    private TestRailReference[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(TestRailReference.List.class))
                       .map(TestRailReference.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(TestRailReference.class)));
    }


    private TestRailReference[] wrapIfNonNull(TestRailReference annotation) {
        return annotation != null ? new TestRailReference[]{annotation} : null;
    }

}
