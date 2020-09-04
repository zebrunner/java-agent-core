package com.zebrunner.agent.core.registrar.label;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface LabelResolver {

    Map<String, List<String>> resolve(Class<?> clazz, Method method);

}
