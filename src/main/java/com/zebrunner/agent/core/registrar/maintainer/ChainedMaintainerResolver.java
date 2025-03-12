package com.zebrunner.agent.core.registrar.maintainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.zebrunner.agent.core.registrar.domain.TestRun;
import com.zebrunner.agent.core.registrar.domain.TestSession;
import com.zebrunner.agent.core.registrar.domain.TestStart;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChainedMaintainerResolver implements MaintainerResolver {

    @Getter
    private static final ChainedMaintainerResolver instance = new ChainedMaintainerResolver();

    private final List<MaintainerResolver> resolvers = new ArrayList<>();

    static {
        ChainedMaintainerResolver.addLast(MethodLevelAnnotationMaintainerResolver.getInstance());
        ChainedMaintainerResolver.addLast(MethodLevelProviderAnnotationMaintainerResolver.getInstance());
        ChainedMaintainerResolver.addLast(ClassLevelAnnotationMaintainerResolver.getInstance());
        ChainedMaintainerResolver.addLast(ClassLevelProviderAnnotationMaintainerResolver.getInstance());
    }

    public static void addFirst(MaintainerResolver resolver) {
        if (!(resolver instanceof ChainedMaintainerResolver)) {
            ChainedMaintainerResolver.getInstance().resolvers.add(0, resolver);
        }
    }

    public static void addLast(MaintainerResolver resolver) {
        if (!(resolver instanceof ChainedMaintainerResolver)) {
            ChainedMaintainerResolver.getInstance().resolvers.add(resolver);
        }
    }

    @Override
    public String resolve(TestRun testRun, TestStart testStart, List<TestSession> testSessions) {
        return resolvers.stream()
                        .map(resolver -> resolver.resolve(testRun, testStart, testSessions))
                        .filter(maintainer -> maintainer != null && !maintainer.trim().isEmpty())
                        .findFirst()
                        .orElse(null);
    }

}
