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

import com.zebrunner.agent.core.annotation.ZephyrTestCaseKey;
import com.zebrunner.agent.core.registrar.descriptor.TestStart;
import com.zebrunner.agent.core.registrar.domain.TcmType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZephyrCasesCollectingRegistrationListener implements RegistrationListener {

    @Getter
    private static final ZephyrCasesCollectingRegistrationListener instance = new ZephyrCasesCollectingRegistrationListener();

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onAfterTestStart(TestStart startDescriptor) {
        ZephyrTestCaseKey[] annotations = this.getAnnotations(startDescriptor.getTestMethod());
        List<String> testCaseIds = Optional.ofNullable(annotations)
                                           .map(Arrays::stream)
                                           .orElseGet(Stream::empty)
                                           .map(ZephyrTestCaseKey::value)
                                           .flatMap(Arrays::stream)
                                           .collect(Collectors.toList());
        testCasesRegistry.addTestCasesToCurrentTest(TcmType.ZEPHYR, testCaseIds);
    }

    private ZephyrTestCaseKey[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(ZephyrTestCaseKey.List.class))
                       .map(ZephyrTestCaseKey.List::value)
                       .orElseGet(() -> this.wrapInArrayIfNonNull(annotatedElement.getAnnotation(ZephyrTestCaseKey.class)));
    }

    private ZephyrTestCaseKey[] wrapInArrayIfNonNull(ZephyrTestCaseKey annotation) {
        return annotation != null ? new ZephyrTestCaseKey[]{annotation} : null;
    }

}
