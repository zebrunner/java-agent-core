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
class BambooCiContextResolver implements CiContextResolver {

    @Getter
    private static final BambooCiContextResolver instance = new BambooCiContextResolver();

    // https://confluence.atlassian.com/bamboo/bamboo-variables-289277087.html#Bamboovariables-Build-specificvariables
    private static final String BAMBOO_BUILD_KEY_ENV_VARIABLE = "bamboo_buildKey";
    private static final List<String> ENV_VARIABLE_PREFIXES = List.of(
            "BAMBOO_",
            "bamboo.",
            "bamboo_"
    );

    @Override
    public CiContext resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(BAMBOO_BUILD_KEY_ENV_VARIABLE)) {
            envVariables = this.collectEnvironmentVariables(envVariables);

            return new CiContext(CiType.TRAVIS_CI, envVariables);
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
