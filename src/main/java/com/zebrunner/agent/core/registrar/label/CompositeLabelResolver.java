package com.zebrunner.agent.core.registrar.label;

import com.zebrunner.agent.core.registrar.domain.LabelDTO;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CompositeLabelResolver implements LabelResolver {

    private static final Set<LabelResolver> RESOLVERS = new HashSet<>();

    static {
        addResolver(new TestLabelResolver());
        addResolver(new PriorityLabelResolver());
        addResolver(new XrayTestKeyLabelResolver());
        addResolver(new TestRailCaseIdLabelResolver());
        addResolver(new ZephyrTestCaseKeyLabelResolver());
    }

    public static void addResolver(LabelResolver labelResolver) {
        if (!(labelResolver instanceof CompositeLabelResolver)) {
            RESOLVERS.add(labelResolver);
        }
    }

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        return RESOLVERS.stream()
                        .map(labelResolver -> labelResolver.resolve(clazz, method))
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

}
