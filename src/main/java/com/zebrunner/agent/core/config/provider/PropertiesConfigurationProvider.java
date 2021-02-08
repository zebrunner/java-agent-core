package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String VALUE_SEPARATORS = "[,;]";

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String PROJECT_KEY_PROPERTY = "reporting.project-key";

    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.access-token";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "reporting.run.display-name";
    private final static String RUN_BUILD_PROPERTY = "reporting.run.build";
    private final static String RUN_ENVIRONMENT_PROPERTY = "reporting.run.environment";

    private final static String RUN_ID_PROPERTY = "reporting.rerun.run-id";

    private final static String NOTIFICATION_SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String NOTIFICATION_MICROSOFT_TEAMS_PROPERTY = "reporting.notification.microsoft-teams-channels";
    private final static String NOTIFICATION_EMAILS_PROPERTY = "reporting.notification.emails";

    private static final String DEFAULT_FILE_NAME = "agent.properties";

    @Override
    public ReportingConfiguration getConfiguration() {
        Properties agentProperties = loadProperties();

        String enabled = agentProperties.getProperty(ENABLED_PROPERTY);
        String projectKey = agentProperties.getProperty(PROJECT_KEY_PROPERTY);
        String hostname = agentProperties.getProperty(HOSTNAME_PROPERTY);
        String accessToken = agentProperties.getProperty(ACCESS_TOKEN_PROPERTY);
        String displayName = agentProperties.getProperty(RUN_DISPLAY_NAME_PROPERTY);
        String build = agentProperties.getProperty(RUN_BUILD_PROPERTY);
        String environment = agentProperties.getProperty(RUN_ENVIRONMENT_PROPERTY);
        String runId = agentProperties.getProperty(RUN_ID_PROPERTY);
        Set<String> slackChannels = getPropertyValueAsSet(agentProperties, NOTIFICATION_SLACK_CHANNELS_PROPERTY, VALUE_SEPARATORS);
        Set<String> microsoftTeamsChannels = getPropertyValueAsSet(agentProperties, NOTIFICATION_MICROSOFT_TEAMS_PROPERTY, VALUE_SEPARATORS);
        Set<String> emails = getPropertyValueAsSet(agentProperties, NOTIFICATION_EMAILS_PROPERTY, VALUE_SEPARATORS);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("Properties configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .reportingEnabled(reportingEnabled)
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .run(new ReportingConfiguration.RunConfiguration(displayName, build, environment))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(slackChannels, microsoftTeamsChannels, emails))
                                     .build();
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

    private Set<String> getPropertyValueAsSet(Properties properties, String key, String separator) {
        String propertyListAsString = properties.getProperty(key);

        if (propertyListAsString == null) {
            return Set.of();
        }

        return Arrays.stream(propertyListAsString.split(separator))
                     .filter(channel -> !channel.isBlank())
                     .map(String::trim)
                     .collect(Collectors.toSet());
    }

}
