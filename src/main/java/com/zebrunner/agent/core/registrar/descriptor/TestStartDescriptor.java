package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Descriptor of test start event. Captures initial test data, such as its name, class and corresponding method,
 * test maintainer and start time
 */
@Getter
@Builder
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class TestStartDescriptor {

    /**
     * Id of already existing Zebrunner test to rerun.
     */
    @Setter
    private Long zebrunnerId;

    /**
     * Contains information about test identity.
     * It might be a JSON with full information about test or hash string which is based pn test properties.
     * The primary use case is to correlate tests on rerun.
     */
    private final String correlationData;

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

    private String testClassName = null;

    /**
     * Test method. This one can be obtained either from test metadata supplied by test framework or
     * by using Java Reflection API. It is needed in order to guarantee test uniqueness within test run and retrieve
     * additional test info (such as maintainer)
     */
    private final Method testMethod;

    private String testMethodName = null;

    /**
     * Index of the arguments passed into test method. If test method does not accept arguments, null value should be provided
     */
    private final Integer argumentsIndex;

    /**
     * Test groups that test belongs to. Test groups allow you to group tests depending on specific criteria and manage them
     * more effectively.
     */
    private final List<String> testGroups;

    public TestStartDescriptor(String correlationData, String name, Class<?> testClass, Method testMethod, Integer argumentsIndex) {
        this(correlationData, name, OffsetDateTime.now(), testClass, testMethod, argumentsIndex, List.of());
    }

    public TestStartDescriptor(String correlationData,
                               String name,
                               OffsetDateTime startedAt,
                               Class<?> testClass,
                               Method testMethod,
                               Integer argumentsIndex) {
        this(correlationData, name, startedAt, testClass, testMethod, argumentsIndex, List.of());
    }

}
