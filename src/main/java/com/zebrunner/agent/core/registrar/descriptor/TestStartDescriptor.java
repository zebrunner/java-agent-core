package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;

/**
 * Descriptor of test start event. Captures initial test data, such as its name, class and corresponding method,
 * test maintainer and start time
 */
@Getter
@ToString
@RequiredArgsConstructor
public class TestStartDescriptor {

    private final String uuid;

    /**
     * Test name. May be different from actual test method name - e.g. a display name.
     * This name will be used to label test in Zebrunner
     */
    private final String name;

    /**
     * Date and time of test start with executor time zone. Ideally it's value should be obtained
     * from test metadata that is supplied by test framework and otherwise tracked manually on agent side.
     */
    private final OffsetDateTime startedAt;

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

}
