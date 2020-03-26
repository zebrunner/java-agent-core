package com.zebrunner.agent.core.registrar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;

/**
 * Descriptor of test start event. Captures initial test data, such as its name, class and corresponding method,
 * test maintainer and start time
 */
@Getter
@AllArgsConstructor
@ToString
public class TestStartDescriptor {

    private String uuid;

    /**
     * Test name. May be different from actual test method name - e.g. a display name.
     * This name will be used to label test in Zebrunner
     */
    private final String name;

    /**
     * Date and time of test start with executor time zone. Ideally it's value should be obtained
     * from test metadata that is supplied by test framework and otherwise tracked manually on agent side.
     */
    private OffsetDateTime startedAt;

    /**
     * A person who is responsible for this test maintenance. This value is optional and can be explicitly set.
     * If it is not set agent will perform a lookup of {@link com.zebrunner.agent.core.reporting.Maintainer} annotation
     * on test methods and classes and will try to derive maintainer from there. Otherwise it will be left empty.
     */
    private String maintainer;

    /**
     * Test class. This one can be obtained either from test metadata supplied by test framework or
     * by using Java Reflection API. It is needed in order to guarantee test uniqueness within test run and retrieve
     * additional test info (such as maintainer)
     */
    private final Class<?> testClass;

    /**
     * Test method. This one can be obtained either from test metadata supplied by test framework or
     * by using Java Reflection API. It is needed in order to guarantee test uniqueness within test run and retrieve
     * additional test info (such as maintainer)
     */
    private final Method testMethod;

    public TestStartDescriptor(String uuid, String name, Class<?> testClass, Method testMethod) {
        this(uuid, name, OffsetDateTime.now(), testClass, testMethod);
    }

    public TestStartDescriptor(String uuid, String name, OffsetDateTime startedAt, Class<?> testClass, Method testMethod) {
        this.uuid = uuid;
        this.name = name;
        this.startedAt = startedAt;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

}
