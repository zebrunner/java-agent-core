package com.zebrunner.agent.core.registrar.label;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.zebrunner.agent.core.annotation.Priority;
import com.zebrunner.agent.core.registrar.domain.Label;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PriorityLabelResolver implements LabelResolver {

    @Getter
    private static final PriorityLabelResolver resolver = new PriorityLabelResolver();

    @Override
    public List<Label> resolve(Class<?> clazz, Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        if (priority == null) {
            priority = clazz.getAnnotation(Priority.class);
        }

        return Optional.ofNullable(priority)
                       .map(Priority::value)
                       .map(value -> new Label("PRIORITY", value))
                       .map(Collections::singletonList)
                       .orElseGet(Collections::emptyList);
    }

}
