package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.annotation.TestCaseKey;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.domain.TcmType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TestCasesCollectingRegistrationListener implements RegistrationListener {

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onAfterTestStart(TestStartDescriptor startDescriptor) {
        TestCaseKey[] annotations = this.getAnnotations(startDescriptor.getTestMethod());
        List<String> testCaseIds = Optional.ofNullable(annotations)
                                           .map(Arrays::stream)
                                           .orElseGet(Stream::empty)
                                           .map(TestCaseKey::value)
                                           .flatMap(Arrays::stream)
                                           .collect(Collectors.toList());
        testCasesRegistry.addTestCasesToCurrentTest(TcmType.ZEBRUNNER, testCaseIds);
    }

    private TestCaseKey[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(TestCaseKey.List.class))
                       .map(TestCaseKey.List::value)
                       .orElseGet(() -> this.wrapInArrayIfNonNull(annotatedElement.getAnnotation(TestCaseKey.class)));
    }

    private TestCaseKey[] wrapInArrayIfNonNull(TestCaseKey annotation) {
        return annotation != null ? new TestCaseKey[]{annotation} : null;
    }

}
