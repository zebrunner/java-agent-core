package com.zebrunner.agent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Repeatable(XrayTestKey.List.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface XrayTestKey {

    String[] value();

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {

        XrayTestKey[] value();

    }

}
