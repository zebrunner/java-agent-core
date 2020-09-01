package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.QTestReference;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QTestReferenceLabelResolver implements LabelResolver {

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        QTestReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        List<String> values = Optional.ofNullable(annotations)
                                      .map(Arrays::stream)
                                      .orElseGet(Stream::empty)
                                      .map(QTestReference::value)
                                      .flatMap(Arrays::stream)
                                      .collect(Collectors.toList());

        return values.isEmpty()
                ? Collections.emptyMap()
                : Collections.singletonMap(Labels.Q_TEST_REFERENCE, values);
    }

    private QTestReference[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(QTestReference.List.class))
                       .map(QTestReference.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(QTestReference.class)));
    }


    private QTestReference[] wrapIfNonNull(QTestReference annotation) {
        return annotation != null ? new QTestReference[]{annotation} : null;
    }

}
