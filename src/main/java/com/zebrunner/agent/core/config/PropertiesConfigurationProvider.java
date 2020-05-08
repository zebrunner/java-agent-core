package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.access-token";
    private final static String RUN_ID_PROPERTY = "reporting.rerun.run-id";

    private static final String DEFAULT_FILE_NAME = "agent.properties";
    private Properties agentProperties;

    @Override
    public ReportingConfiguration getConfiguration() {

        if (agentProperties == null) {
            agentProperties = loadProperties();
        }

        String enabled = agentProperties.getProperty(ENABLED_PROPERTY);
        String hostname = agentProperties.getProperty(HOSTNAME_PROPERTY);
        String accessToken = agentProperties.getProperty(ACCESS_TOKEN_PROPERTY);
        String runId = agentProperties.getProperty(RUN_ID_PROPERTY);

        boolean enabledIsBoolean = enabled == null
                || String.valueOf(true).equalsIgnoreCase(enabled)
                || String.valueOf(false).equalsIgnoreCase(enabled);
        if (!enabledIsBoolean) {
            throw new TestAgentException("Properties configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .enabled(reportingEnabled)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId)).build();
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream resource = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME)) {
            if (resource != null) {
                properties.load(resource);
            }
        } catch (IOException e) {
            throw new TestAgentException("Unable to load agent configuration from properties file");
        }
        return properties;
    }

}
