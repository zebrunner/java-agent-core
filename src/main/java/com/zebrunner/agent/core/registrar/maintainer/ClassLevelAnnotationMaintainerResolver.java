package com.zebrunner.agent.core.registrar.maintainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

import com.zebrunner.agent.core.annotation.Maintainer;
import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.domain.TestRun;
import com.zebrunner.agent.core.registrar.domain.TestSession;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassLevelAnnotationMaintainerResolver implements MaintainerResolver {

    @Getter
    private static final ClassLevelAnnotationMaintainerResolver instance = new ClassLevelAnnotationMaintainerResolver();

    @Override
    public String resolve(TestRun testRun, TestStart testStart, List<TestSession> testSessions) {
        return Optional.ofNullable(testStart.getTestClass())
                       .map(testClass -> testClass.getAnnotation(Maintainer.class))
                       .map(Maintainer::value)
                       .orElse(null);
    }

}
