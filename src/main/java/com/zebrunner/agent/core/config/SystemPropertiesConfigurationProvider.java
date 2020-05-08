package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

public class SystemPropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.accessToken";
    private final static String RUN_ID_PROPERTY = "reporting.rerun.runId";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getProperty(ENABLED_PROPERTY);
        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        String accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY);
        String runId = System.getProperty(RUN_ID_PROPERTY);

        boolean enabledIsBoolean = enabled == null
                || String.valueOf(true).equalsIgnoreCase(enabled)
                || String.valueOf(false).equalsIgnoreCase(enabled);
        if (!enabledIsBoolean) {
            throw new TestAgentException("System properties configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .enabled(reportingEnabled)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId)).build();
    }

}
