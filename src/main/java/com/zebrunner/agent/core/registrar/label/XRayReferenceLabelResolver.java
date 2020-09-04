package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.XRayReference;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XRayReferenceLabelResolver implements LabelResolver {

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        XRayReference[] annotations = getAnnotations(method);
        if (annotations == null) {
            annotations = getAnnotations(clazz);
        }

        List<String> values = Optional.ofNullable(annotations)
                                      .map(Arrays::stream)
                                      .orElseGet(Stream::empty)
                                      .map(XRayReference::value)
                                      .flatMap(Arrays::stream)
                                      .collect(Collectors.toList());

        return values.isEmpty()
                ? Collections.emptyMap()
                : Collections.singletonMap(Labels.X_RAY_REFERENCE, values);
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
