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
class TeamCityCiContextResolver implements CiContextResolver {

    @Getter
    private static final TeamCityCiContextResolver instance = new TeamCityCiContextResolver();

    // https://www.jetbrains.com/help/teamcity/predefined-build-parameters.html#Server+Build+Properties
    private static final String TEAMCITY_VERSION_ENV_VARIABLE = "TEAMCITY_VERSION";
    private static final List<String> ENV_VARIABLE_PREFIXES = List.of(
            "BUILD_",
            "HOSTNAME",
            "SERVER_URL",
            "TEAMCITY_"
    );

    @Override
    public CiContext resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(TEAMCITY_VERSION_ENV_VARIABLE)) {
            envVariables = this.collectEnvironmentVariables(envVariables);

            return new CiContext(CiType.TEAM_CITY, envVariables);
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
