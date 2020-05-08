package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_VARIABLE = "REPORTING_SERVER_HOSTNAME";
    private final static String HOSTNAME_VARIABLE = "REPORTING_ENABLED";
    private final static String ACCESS_TOKEN_VARIABLE = "REPORTING_SERVER_ACCESS_TOKEN";
    private final static String RUN_ID_VARIABLE = "REPORTING_RERUN_RUN_ID";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getenv(ENABLED_VARIABLE);
        String hostname = System.getenv(HOSTNAME_VARIABLE);
        String accessToken = System.getenv(ACCESS_TOKEN_VARIABLE);
        String runId = System.getenv(RUN_ID_VARIABLE);

        boolean enabledIsBoolean = enabled == null
                || String.valueOf(true).equalsIgnoreCase(enabled)
                || String.valueOf(false).equalsIgnoreCase(enabled);
        if (!enabledIsBoolean) {
            throw new TestAgentException("Environment configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .enabled(reportingEnabled)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId)).build();
    }

}
