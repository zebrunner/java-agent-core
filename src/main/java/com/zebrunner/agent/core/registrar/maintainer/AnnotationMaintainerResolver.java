package com.zebrunner.agent.core.registrar.maintainer;

import com.zebrunner.agent.core.annotation.Maintainer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class AnnotationMaintainerResolver implements MaintainerResolver {

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
