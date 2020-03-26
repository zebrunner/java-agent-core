package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFileConfigurationProvider implements ConfigurationProvider {

    private final static String HOSTNAME_PROPERTY = "zbr.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "zbr.access-token";
    private final static String RUN_ID = "run_id";

    private static final String DEFAULT_FILE_NAME = "agent.properties";

    @Override
    public Configuration getConfiguration() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME);
        if (inputStream == null) {
            throw new TestAgentException("Unable to load agent configuration from properties file");
        }

        Properties accountProperties = new Properties();
        try {
            accountProperties.load(inputStream);
        } catch (IOException e) {
            throw new TestAgentException("Unable to load agent configuration from properties file");
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }

        String hostname = accountProperties.getProperty(HOSTNAME_PROPERTY);
        String accessToken = accountProperties.getProperty(ACCESS_TOKEN_PROPERTY);
        String runId = accountProperties.getProperty(RUN_ID);
        if (hostname == null || accessToken == null) {
            throw new TestAgentException("Configuration file lacks mandatory properties");
        }

        return new Configuration(hostname, accessToken, runId);
    }

}
