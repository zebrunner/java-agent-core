package com.zebrunner.agent.core.registrar.ci;

import com.zebrunner.agent.core.registrar.domain.CiContextDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompositeCiContextResolver implements CiContextResolver {

    private static final CompositeCiContextResolver INSTANCE = new CompositeCiContextResolver();

    public static CompositeCiContextResolver getInstance() {
        return INSTANCE;
    }

    private final List<CiContextResolver> ciContextResolvers = Arrays.asList(
            new JenkinsCiContextResolver()
    );

    @Override
    public CiContextDTO resolve() {
        return ciContextResolvers.stream()
                                 .map(CiContextResolver::resolve)
                                 .filter(Objects::nonNull)
                                 .findFirst()
                                 .orElse(null);
    }

}
