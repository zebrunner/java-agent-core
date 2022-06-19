package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.annotation.TestRailCaseId;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.domain.TcmType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TestRailCasesCollectingRegistrationListener implements RegistrationListener {

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onAfterTestStart(TestStartDescriptor startDescriptor) {
        TestRailCaseId[] annotations = this.getAnnotations(startDescriptor.getTestMethod());
        List<String> testCaseIds = Optional.ofNullable(annotations)
                                           .map(Arrays::stream)
                                           .orElseGet(Stream::empty)
                                           .map(TestRailCaseId::value)
                                           .flatMap(Arrays::stream)
                                           .collect(Collectors.toList());
        testCasesRegistry.addTestCasesToCurrentTest(TcmType.TEST_RAIL, testCaseIds);
    }

    private TestRailCaseId[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(TestRailCaseId.List.class))
                       .map(TestRailCaseId.List::value)
                       .orElseGet(() -> this.wrapInArrayIfNonNull(annotatedElement.getAnnotation(TestRailCaseId.class)));
    }

    private TestRailCaseId[] wrapInArrayIfNonNull(TestRailCaseId annotation) {
        return annotation != null ? new TestRailCaseId[]{annotation} : null;
    }

}
