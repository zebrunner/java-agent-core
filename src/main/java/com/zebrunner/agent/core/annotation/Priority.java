package com.zebrunner.agent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Priority {

    String P1 = "P1";
    String P2 = "P2";
    String P3 = "P3";
    String P4 = "P4";
    String P5 = "P5";
    String P6 = "P6";

    String value();

}
