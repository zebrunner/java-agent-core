package com.zebrunner.agent.core.registrar.ci;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zebrunner.agent.core.registrar.domain.CiContext;
import com.zebrunner.agent.core.registrar.domain.CiType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CircleCiContextResolver implements CiContextResolver {

    @Getter
    private static final CircleCiContextResolver instance = new CircleCiContextResolver();

    // https://circleci.com/docs/2.0/env-vars/#built-in-environment-variables
    private static final String CIRCLECI_ENV_VARIABLE = "CIRCLECI";
    private static final List<String> ENV_VARIABLE_PREFIXES = List.of(
            "CIRCLE",
            "HOSTNAME"
    );

    @Override
    public CiContext resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(CIRCLECI_ENV_VARIABLE)) {
            envVariables = this.collectEnvironmentVariables(envVariables);

            return new CiContext(CiType.CIRCLE_CI, envVariables);
        }

        return null;
    }

    private Map<String, String> collectEnvironmentVariables(Map<String, String> envVariables) {
        return envVariables.keySet()
                           .stream()
                           .filter(key -> ENV_VARIABLE_PREFIXES.stream().anyMatch(key::startsWith))
                           .collect(Collectors.toMap(Function.identity(), envVariables::get));
    }

}
