package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.Priority;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PriorityLabelResolver implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        if (priority == null) {
            priority = clazz.getAnnotation(Priority.class);
        }

        return Optional.ofNullable(priority)
                       .map(Priority::value)
                       .map(value -> new LabelDTO(Labels.PRIORITY, value))
                       .map(Collections::singletonList)
                       .orElseGet(Collections::emptyList);
    }

}
