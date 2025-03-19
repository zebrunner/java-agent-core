package com.zebrunner.agent.core.registrar.maintainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

import com.zebrunner.agent.core.annotation.MaintainerProvider;
import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.domain.TestRun;
import com.zebrunner.agent.core.registrar.domain.TestSession;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodLevelProviderAnnotationMaintainerResolver extends BaseProviderAnnotationMaintainerResolver {

    @Getter
    private static final MethodLevelProviderAnnotationMaintainerResolver instance = new MethodLevelProviderAnnotationMaintainerResolver();

    @Override
    public String resolve(TestRun testRun, TestStart testStart, List<TestSession> testSessions) {
        return Optional.ofNullable(testStart.getTestMethod())
                       .map(testMethod -> testMethod.getAnnotation(MaintainerProvider.class))
                       .map(MaintainerProvider::value)
                       .map(this::constructProviderInstance)
                       .map(provider -> provider.provide(testRun, testStart, testSessions))
                       .orElse(null);
    }

}
