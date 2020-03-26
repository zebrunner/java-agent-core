package com.zebrunner.agent.core.reporting;

import java.lang.reflect.Method;

public final class MaintainerProcessor {

    private MaintainerProcessor() {
    }

    /**
     * Retrieves owner from {@link Maintainer} annotation declared on test class or on test method
     * Priorities:
     * 1. Test method owner
     * 2. Test class owner
     * 3. Suite owner
     * @param klass test class
     * @param method test method
     * @return maintainer name, email or id
     */
    public static String retrieveOwner(Class<?> klass, Method method) {
        Maintainer maintainer = null;

        if (method != null && method.isAnnotationPresent(Maintainer.class)) {
            maintainer = method.getDeclaredAnnotation(Maintainer.class);
        } else if (klass != null && klass.isAnnotationPresent(Maintainer.class)) {
            maintainer = klass.getDeclaredAnnotation(Maintainer.class);
        }

        return maintainer != null ? maintainer.value() : null;
    }

}
