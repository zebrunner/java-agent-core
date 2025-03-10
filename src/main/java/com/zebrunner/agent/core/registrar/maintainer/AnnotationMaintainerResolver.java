package com.zebrunner.agent.core.registrar.maintainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

import com.zebrunner.agent.core.annotation.Maintainer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationMaintainerResolver implements MaintainerResolver {

    @Getter
    private static final AnnotationMaintainerResolver instance = new AnnotationMaintainerResolver();

    @Override
    public String resolve(Class<?> klass, Method method) {
        Maintainer maintainer = null;

        if (method != null && method.isAnnotationPresent(Maintainer.class)) {
            maintainer = method.getDeclaredAnnotation(Maintainer.class);
        } else if (klass != null && klass.isAnnotationPresent(Maintainer.class)) {
            maintainer = klass.getAnnotation(Maintainer.class);
        }

        return maintainer != null ? maintainer.value() : null;
    }

}
