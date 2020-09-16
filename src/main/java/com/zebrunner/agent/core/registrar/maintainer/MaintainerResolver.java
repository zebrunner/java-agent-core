package com.zebrunner.agent.core.registrar.maintainer;

import java.lang.reflect.Method;

public interface MaintainerResolver {

    String resolve(Class<?> clazz, Method method);

}
