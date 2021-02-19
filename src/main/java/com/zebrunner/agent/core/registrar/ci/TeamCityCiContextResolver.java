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
class TeamCityCiContextResolver implements CiContextResolver {

    // https://www.jetbrains.com/help/teamcity/predefined-build-parameters.html#Server+Build+Properties
    private static final String TEAMCITY_VERSION_ENV_VARIABLE = "TEAMCITY_VERSION";
    private static final List<String> ENV_VARIABLE_PREFIXES = Arrays.asList(
            "BUILD_",
            "HOSTNAME",
            "SERVER_URL",
            "TEAMCITY_"
    );

    @Override
    public CiContextDTO resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(TEAMCITY_VERSION_ENV_VARIABLE)) {
            envVariables = collectEnvironmentVariables(envVariables);
            return new CiContextDTO(CiType.TEAM_CITY, envVariables);
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
