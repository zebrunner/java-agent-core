package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.zebrunner.agent.core.annotation.TestCaseKey;
import com.zebrunner.agent.core.registrar.descriptor.TestStart;
import com.zebrunner.agent.core.registrar.domain.TcmType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TestCasesCollectingRegistrationListener implements RegistrationListener {

    @Getter
    private static final TestCasesCollectingRegistrationListener instance = new TestCasesCollectingRegistrationListener();

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onAfterTestStart(TestStart startDescriptor) {
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
