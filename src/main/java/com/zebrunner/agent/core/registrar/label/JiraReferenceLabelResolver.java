package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.JiraReference;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JiraReferenceLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        JiraReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(JiraReference::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(Labels.JIRA_REFERENCE, value))
                       .collect(Collectors.toList());
    }

    private JiraReference[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(JiraReference.List.class))
                       .map(JiraReference.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(JiraReference.class)));
    }


    private JiraReference[] wrapIfNonNull(JiraReference annotation) {
        return annotation != null ? new JiraReference[]{annotation} : null;
    }

}
