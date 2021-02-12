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
class JenkinsCiContextResolver implements CiContextResolver {

    // https://wiki.jenkins.io/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-below
    private static final String JENKINS_URL_ENV_VARIABLE = "JENKINS_URL";
    private static final List<String> COLLECTABLE_ENV_VARIABLES = Arrays.asList(
            "BUILD_ID",
            "BUILD_NUMBER",
            "BUILD_TAG",
            "BUILD_URL",
            "SVN_REVISION",
            "CVS_BRANCH",
            "GIT_COMMIT",
            "GIT_BRANCH",
            "GIT_URL",
            "JAVA_HOME",
            "NODE_NAME",
            "JOB_NAME",
            JENKINS_URL_ENV_VARIABLE,
            "EXECUTOR_NUMBER",
            "WORKSPACE"
    );

    @Override
    public CiContextDTO resolve() {
        Map<String, String> envVariables = System.getenv();

        if (envVariables.containsKey(JENKINS_URL_ENV_VARIABLE)) {
            envVariables = collectEnvironmentVariables(envVariables);
            return new CiContextDTO(CiType.JENKINS, envVariables);
        }

        return null;
    }

    private Map<String, String> collectEnvironmentVariables(Map<String, String> envVariables) {
        return COLLECTABLE_ENV_VARIABLES.stream()
                                        .filter(envVariables::containsKey)
                                        .collect(Collectors.toMap(Function.identity(), envVariables::get));
    }

}
