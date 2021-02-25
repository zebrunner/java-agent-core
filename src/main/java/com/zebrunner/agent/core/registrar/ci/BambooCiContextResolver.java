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
class BambooCiContextResolver implements CiContextResolver {

    // https://confluence.atlassian.com/bamboo/bamboo-variables-289277087.html#Bamboovariables-Build-specificvariables
    private static final String BAMBOO_BUILD_KEY_ENV_VARIABLE = "bamboo_buildKey";
    private static final List<String> ENV_VARIABLE_PREFIXES = Arrays.asList(
            "BAMBOO_",
            "bamboo.",
            "bamboo_"
    );

    @Override
    public CiContextDTO resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(BAMBOO_BUILD_KEY_ENV_VARIABLE)) {
            envVariables = collectEnvironmentVariables(envVariables);
            return new CiContextDTO(CiType.TRAVIS_CI, envVariables);
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
