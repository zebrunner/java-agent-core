package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.TestRailReference;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRailReferenceLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        TestRailReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(TestRailReference::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(Labels.TEST_RAIL_REFERENCE, value))
                       .collect(Collectors.toList());
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
