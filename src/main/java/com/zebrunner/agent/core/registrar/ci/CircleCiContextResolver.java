package com.zebrunner.agent.core.registrar.ci;

import com.zebrunner.agent.core.registrar.domain.CiContextDTO;
import com.zebrunner.agent.core.registrar.domain.CiType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
class CircleCiContextResolver implements CiContextResolver {

    // https://circleci.com/docs/2.0/env-vars/#built-in-environment-variables
    private static final String CIRCLECI_ENV_VARIABLE = "CIRCLECI";
    private static final List<String> ENV_VARIABLE_PREFIXES = Arrays.asList(
            "CIRCLE",
            "HOSTNAME"
    );

    @Override
    public CiContextDTO resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(CIRCLECI_ENV_VARIABLE)) {
            envVariables = collectEnvironmentVariables(envVariables);
            return new CiContextDTO(CiType.CIRCLE_CI, envVariables);
        }

        return null;
    }

    private Map<String, String> collectEnvironmentVariables(Map<String, String> envVariables) {
        return envVariables.keySet()
                           .stream()
                           .filter(key -> ENV_VARIABLE_PREFIXES.stream()
                                                               .anyMatch(key::startsWith))
                           .collect(Collectors.toMap(Function.identity(), envVariables::get));
    }

}
