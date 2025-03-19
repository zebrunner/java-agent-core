package com.zebrunner.agent.core.registrar.maintainer;

import java.lang.reflect.InvocationTargetException;

public abstract class BaseProviderAnnotationMaintainerResolver implements MaintainerResolver {

    MaintainerProvider constructProviderInstance(Class<? extends MaintainerProvider> provider) {
        try {
            return provider.getDeclaredConstructor()
                           .newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Could not instantiate MaintainerProvider implementation of type ''" + provider, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Could not instantiate MaintainerProvider implementation of type ''" + provider
                    + ": this class does not contain a constructor without arguments",
                    e
            );
        }
    }

}
