package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.Method;
import java.util.List;

public interface LabelResolver {

    List<LabelDTO> resolve(Class<?> clazz, Method method);

}
