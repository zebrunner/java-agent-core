package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    private final static String HOSTNAME_VARIABLE = "ZBR_HOSTNAME";
    private final static String ACCESS_TOKEN_VARIABLE = "ZBR_ACCESS_TOKEN";
    private final static String RUN_ID = "RUN_ID";

    @Override
    public Configuration getConfiguration() {
        String hostname = System.getenv(HOSTNAME_VARIABLE);
        String accessToken = System.getenv(ACCESS_TOKEN_VARIABLE);
        String runId = System.getenv(RUN_ID);

        hostname = hostname != null ? hostname.trim() : null;
        accessToken = accessToken != null ? accessToken.trim() : null;
        runId = runId != null ? runId.trim() : null;

        if (hostname == null || hostname.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            throw new TestAgentException("Unable to load agent configuration from environment");
        }

        return new Configuration(hostname, accessToken, runId);
    }

}
