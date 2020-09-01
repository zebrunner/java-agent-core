package com.zebrunner.agent.core.registrar.label;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompositeLabelResolver implements LabelResolver {

    private final List<LabelResolver> downstreamResolvers = Arrays.asList(
            new TestLabelResolver(),
            new PriorityLabelResolver(),
            new JiraReferenceLabelResolver(),
            new XRayReferenceLabelResolver(),
            new QTestReferenceLabelResolver(),
            new TestRailReferenceLabelResolver()
    );

    @Override
    public Map<String, List<String>> resolve(Class<?> clazz, Method method) {
        return downstreamResolvers.stream()
                                  .map(labelResolver -> labelResolver.resolve(clazz, method))
                                  .filter(Objects::nonNull)
                                  .flatMap(map -> map.entrySet().stream())
                                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
