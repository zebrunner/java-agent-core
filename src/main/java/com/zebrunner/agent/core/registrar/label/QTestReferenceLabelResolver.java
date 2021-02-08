package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.QTestReference;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QTestReferenceLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        QTestReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(QTestReference::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(Labels.Q_TEST_REFERENCE, value))
                       .collect(Collectors.toList());
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
