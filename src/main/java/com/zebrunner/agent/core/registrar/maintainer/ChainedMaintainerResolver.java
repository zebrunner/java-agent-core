package com.zebrunner.agent.core.registrar.maintainer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ChainedMaintainerResolver implements MaintainerResolver {

    private static final List<MaintainerResolver> resolvers = new ArrayList<>();

    static {
        addFirst(new AnnotationMaintainerResolver());
    }

    public static void addFirst(MaintainerResolver resolver) {
        resolvers.add(0, resolver);
    }

    public static void addLast(MaintainerResolver resolver) {
        resolvers.add(resolver);
    }

    @Override
    public String resolve(Class<?> clazz, Method method) {
        return resolvers.stream()
                        .map(resolver -> resolver.resolve(clazz, method))
                        .filter(maintainer -> maintainer != null && !maintainer.trim().isEmpty())
                        .findFirst()
                        .orElse(null);
    }

}
