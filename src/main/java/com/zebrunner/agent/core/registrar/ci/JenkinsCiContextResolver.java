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
class JenkinsCiContextResolver implements CiContextResolver {

    @Getter
    private static final JenkinsCiContextResolver instance = new JenkinsCiContextResolver();

    // https://wiki.jenkins.io/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-below
    private static final String JENKINS_URL_ENV_VARIABLE = "JENKINS_URL";
    private static final List<String> ENV_VARIABLE_PREFIXES = List.of(
            "CVS_",
            "SVN_",
            "GIT_",
            "NODE_",
            "EXECUTOR_NUMBER",
            "JENKINS_",
            "JOB_",
            "BUILD_",
            "ROOT_BUILD_",
            "RUN_",
            "WORKSPACE"
    );

    @Override
    public CiContext resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(JENKINS_URL_ENV_VARIABLE)) {
            envVariables = this.collectEnvironmentVariables(envVariables);

            return new CiContext(CiType.JENKINS, envVariables);
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
