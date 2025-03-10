package com.zebrunner.agent.core.registrar.ci;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

import com.zebrunner.agent.core.registrar.domain.CiContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompositeCiContextResolver implements CiContextResolver {

    @Getter
    private static final CompositeCiContextResolver instance = new CompositeCiContextResolver();

    private final List<CiContextResolver> resolvers = List.of(
            JenkinsCiContextResolver.getInstance(),
            TeamCityCiContextResolver.getInstance(),
            CircleCiContextResolver.getInstance(),
            TravisCiContextResolver.getInstance(),
            BambooCiContextResolver.getInstance()
    );

    @Override
    public CiContext resolve() {
        return resolvers.stream()
                        .map(CiContextResolver::resolve)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
    }

}
