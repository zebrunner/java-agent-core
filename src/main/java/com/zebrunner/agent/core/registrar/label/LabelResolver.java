package com.zebrunner.agent.core.registrar.label;

import java.lang.reflect.Method;
import java.util.List;

import com.zebrunner.agent.core.registrar.domain.Label;

public interface LabelResolver {

    static LabelResolver getInstance() {
        return CompositeLabelResolver.getInstance();
    }

    List<Label> resolve(Class<?> clazz, Method method);

}
