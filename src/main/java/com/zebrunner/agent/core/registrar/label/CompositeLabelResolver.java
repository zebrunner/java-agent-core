package com.zebrunner.agent.core.registrar.label;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CompositeLabelResolver implements LabelResolver {

    private static final Set<LabelResolver> RESOLVERS = new HashSet<>();

    static {
        addResolver(new TestLabelResolver());
        addResolver(new PriorityLabelResolver());
        addResolver(new JiraReferenceLabelResolver());
        addResolver(new XRayReferenceLabelResolver());
        addResolver(new QTestReferenceLabelResolver());
        addResolver(new TestRailReferenceLabelResolver());
    }

    public static void addResolver(LabelResolver labelResolver) {
        if (!(labelResolver instanceof CompositeLabelResolver)) {
            RESOLVERS.add(labelResolver);
        }
    }

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        return RESOLVERS.stream()
                        .map(labelResolver -> labelResolver.resolve(clazz, method))
                        .filter(Objects::nonNull)
                        .flatMap(map -> map.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
