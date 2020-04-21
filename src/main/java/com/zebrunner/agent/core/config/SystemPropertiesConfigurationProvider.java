package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

public class SystemPropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String HOSTNAME_PROPERTY = "zbr.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "zbr.accessToken";
    private final static String RUN_ID = "run_id";

    @Override
    public Configuration getConfiguration() {
        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        String accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY);
        String runId = System.getProperty(RUN_ID);

        hostname = hostname != null ? hostname.trim() : null;
        accessToken = accessToken != null ? accessToken.trim() : null;
        runId = runId != null ? runId.trim() : null;

        if (hostname == null || hostname.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            throw new TestAgentException("Unable to load agent configuration from system properties");
        }

        return new Configuration(hostname, accessToken, runId);
    }

}
