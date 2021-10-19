package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.ZephyrTestCaseKey;
import com.zebrunner.agent.core.registrar.Zephyr;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZephyrTestCaseKeyLabelResolver implements LabelResolver {

    public ZephyrTestCaseKeyLabelResolver() {
    }

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        ZephyrTestCaseKey[] annotations = getAnnotations(method);
        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(ZephyrTestCaseKey::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(Zephyr.TEST_CASE_KEY, value))
                       .collect(Collectors.toList());
    }

    private ZephyrTestCaseKey[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(ZephyrTestCaseKey.List.class))
                       .map(ZephyrTestCaseKey.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(ZephyrTestCaseKey.class)));
    }

    private ZephyrTestCaseKey[] wrapIfNonNull(ZephyrTestCaseKey annotation) {
        return annotation != null ? new ZephyrTestCaseKey[]{annotation} : null;
    }

}
