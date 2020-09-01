package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.annotation.Priority;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PriorityLabelResolver implements LabelResolver {

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        if (priority == null) {
            priority = clazz.getAnnotation(Priority.class);
        }

        return priority != null
                ? Collections.singletonMap(Labels.PRIORITY, Collections.singletonList(priority.value()))
                : Collections.emptyMap();
    }

}
