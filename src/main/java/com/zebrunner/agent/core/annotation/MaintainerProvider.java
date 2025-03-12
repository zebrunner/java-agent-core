package com.zebrunner.agent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MaintainerProvider {

    Class<? extends com.zebrunner.agent.core.registrar.maintainer.MaintainerProvider> value();

}
