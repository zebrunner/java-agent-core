package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class Test {

    private String uuid;
    private final String name;
    private String maintainer;
    private final Class<?> testClass;
    private final Method testMethod;

    public Test(String uuid, String name, Class<?> testClass, Method testMethod) {
        this.uuid = uuid;
        this.name = name;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public Test(String name, Class<?> testClass, Method testMethod) {
        this.name = name;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }
}
