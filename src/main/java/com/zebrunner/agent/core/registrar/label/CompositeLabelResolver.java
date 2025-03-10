package com.zebrunner.agent.core.registrar.label;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.zebrunner.agent.core.registrar.domain.Label;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompositeLabelResolver implements LabelResolver {

    @Getter
    private static final CompositeLabelResolver instance = new CompositeLabelResolver();

    private final Set<LabelResolver> resolvers = new HashSet<>();

    static {
        CompositeLabelResolver.addResolver(TestLabelResolver.getInstance());
        CompositeLabelResolver.addResolver(PriorityLabelResolver.getResolver());
    }

    public static void addResolver(LabelResolver labelResolver) {
        if (!(labelResolver instanceof CompositeLabelResolver)) {
            CompositeLabelResolver.getInstance().resolvers.add(labelResolver);
        }
    }

    @Override
    public List<Label> resolve(Class<?> clazz, Method method) {
        return resolvers.stream()
                        .map(labelResolver -> labelResolver.resolve(clazz, method))
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

}
