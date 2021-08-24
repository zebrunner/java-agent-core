package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.XrayTestKey;
import com.zebrunner.agent.core.registrar.Xray;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XrayTestKeyLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        XrayTestKey[] annotations = getAnnotations(method);
        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(XrayTestKey::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(Xray.TEST_KEY, value))
                       .collect(Collectors.toList());
    }

    private XrayTestKey[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(XrayTestKey.List.class))
                       .map(XrayTestKey.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(XrayTestKey.class)));
    }

    private XrayTestKey[] wrapIfNonNull(XrayTestKey annotation) {
        return annotation != null ? new XrayTestKey[]{annotation} : null;
    }

}
