package com.zebrunner.agent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TestRailReference.List.class)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestRailReference {

    String[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface List {
        TestRailReference[] value();
    }

}
