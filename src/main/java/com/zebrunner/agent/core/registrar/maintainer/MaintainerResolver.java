package com.zebrunner.agent.core.registrar.maintainer;

import java.lang.reflect.Method;

public interface MaintainerResolver {

    static MaintainerResolver getInstance() {
        return ChainedMaintainerResolver.getInstance();
    }

    String resolve(Class<?> clazz, Method method);

}
