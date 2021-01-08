package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.XRayReference;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XRayReferenceLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        XRayReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        return Optional.ofNullable(annotations)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .map(XRayReference::value)
                       .flatMap(Arrays::stream)
                       .map(value -> new LabelDTO(Labels.X_RAY_REFERENCE, value))
                       .collect(Collectors.toList());
    }

    private XRayReference[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(XRayReference.List.class))
                       .map(XRayReference.List::value)
                       .orElseGet(() -> wrapIfNonNull(annotatedElement.getAnnotation(XRayReference.class)));
    }


    private XRayReference[] wrapIfNonNull(XRayReference annotation) {
        return annotation != null ? new XRayReference[]{annotation} : null;
    }

}
