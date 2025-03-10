package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class TestStart {

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

    private final Instant startedAt;

    private final Class<?> testClass;
    private String testClassName = null;

    private final Method testMethod;
    private String testMethodName = null;

    /**
     * Index of the arguments passed into test method. If test method does not accept arguments, null value should be provided
     */
    private final Integer argumentsIndex;

    private final List<String> testGroups;

    public TestStart(String correlationData, String name, Class<?> testClass, Method testMethod, Integer argumentsIndex) {
        this(correlationData, name, Instant.now(), testClass, testMethod, argumentsIndex, List.of());
    }

    public TestStart(String correlationData,
                     String name,
                     Instant startedAt,
                     Class<?> testClass,
                     Method testMethod,
                     Integer argumentsIndex) {
        this(correlationData, name, startedAt, testClass, testMethod, argumentsIndex, List.of());
    }

}
