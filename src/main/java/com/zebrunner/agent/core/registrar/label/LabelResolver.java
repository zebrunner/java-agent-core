package com.zebrunner.agent.core.registrar.label;

import java.lang.reflect.Method;
import java.util.List;

import com.zebrunner.agent.core.registrar.domain.Label;

public interface LabelResolver {

    static LabelResolver getInstance() {
        return CompositeLabelResolver.getInstance();
    }

    // '? extends' is added for backward compatibility with carina
    List<? extends Label> resolve(Class<?> clazz, Method method);

}
