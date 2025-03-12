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

import com.zebrunner.agent.core.annotation.XrayTestKey;
import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.domain.TcmType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class XrayCasesCollectingRegistrationListener implements RegistrationListener {

    @Getter
    private static final XrayCasesCollectingRegistrationListener instance = new XrayCasesCollectingRegistrationListener();

    private final TestCasesRegistry testCasesRegistry = TestCasesRegistry.getInstance();

    @Override
    public void onAfterTestStart(TestStart startDescriptor) {
        XrayTestKey[] annotations = this.getAnnotations(startDescriptor.getTestMethod());
        List<String> testCaseIds = Optional.ofNullable(annotations)
                                           .map(Arrays::stream)
                                           .orElseGet(Stream::empty)
                                           .map(XrayTestKey::value)
                                           .flatMap(Arrays::stream)
                                           .collect(Collectors.toList());
        testCasesRegistry.addTestCasesToCurrentTest(TcmType.XRAY, testCaseIds);
    }

    private XrayTestKey[] getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(XrayTestKey.List.class))
                       .map(XrayTestKey.List::value)
                       .orElseGet(() -> this.wrapInArrayIfNonNull(annotatedElement.getAnnotation(XrayTestKey.class)));
    }

    private XrayTestKey[] wrapInArrayIfNonNull(XrayTestKey annotation) {
        return annotation != null ? new XrayTestKey[]{annotation} : null;
    }

}
