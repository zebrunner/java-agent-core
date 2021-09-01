package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.TestRailCaseId;
import com.zebrunner.agent.core.registrar.TestRail;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRailCaseIdLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        TestRailCaseId[] annotations = getAnnotations(method);
        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(TestRailCaseId::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(TestRail.CASE_ID, value))
                       .collect(Collectors.toList());
    }

    private TestRailCaseId[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(TestRailCaseId.List.class))
                       .map(TestRailCaseId.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(TestRailCaseId.class)));
    }

    private TestRailCaseId[] wrapIfNonNull(TestRailCaseId annotation) {
        return annotation != null ? new TestRailCaseId[]{annotation} : null;
    }

}
