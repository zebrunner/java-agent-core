package com.zebrunner.agent.core.registrar.maintainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChainedMaintainerResolver implements MaintainerResolver {

    @Getter
    private static final ChainedMaintainerResolver instance = new ChainedMaintainerResolver();

    private final List<MaintainerResolver> resolvers = new ArrayList<>();

    {
        this.addFirst(AnnotationMaintainerResolver.getInstance());
    }

    public void addFirst(MaintainerResolver resolver) {
        if (!(resolver instanceof ChainedMaintainerResolver)) {
            resolvers.add(0, resolver);
        }
    }

    public void addLast(MaintainerResolver resolver) {
        if (!(resolver instanceof ChainedMaintainerResolver)) {
            resolvers.add(resolver);
        }
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
