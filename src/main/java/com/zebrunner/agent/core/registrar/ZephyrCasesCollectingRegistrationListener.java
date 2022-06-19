package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.annotation.ZephyrTestCaseKey;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.domain.TcmType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ZephyrCasesCollectingRegistrationListener implements RegistrationListener {

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onAfterTestStart(TestStartDescriptor startDescriptor) {
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
