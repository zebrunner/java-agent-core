package com.zebrunner.agent.core.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

interface Configuration<T extends Configuration<T>> {

    void copyMissing(T providedConfig);

    default <C> void setIfNull(Supplier<C> value, Supplier<C> defaultValue, Consumer<C> setter) {
        if (value.get() == null) {
            setter.accept(defaultValue.get());
        }
    }

}
