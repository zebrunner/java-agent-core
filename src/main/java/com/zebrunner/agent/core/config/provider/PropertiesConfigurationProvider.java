package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.zebrunner.agent.core.config.ConfigurationUtils.parseBoolean;

public class PropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String PROJECT_KEY_PROPERTY = "reporting.project-key";

    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.access-token";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "reporting.run.display-name";
    private final static String RUN_BUILD_PROPERTY = "reporting.run.build";
    private final static String RUN_ENVIRONMENT_PROPERTY = "reporting.run.environment";
    private final static String RUN_CONTEXT_PROPERTY = "reporting.run.context";
    private final static String RUN_RETRY_KNOWN_ISSUES_PROPERTY = "reporting.run.retry-known-issues";
    private final static String RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_PROPERTY = "reporting.run.substitute-remote-web-drivers";

    private final static String NOTIFICATION_SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String NOTIFICATION_MS_TEAMS_PROPERTY = "reporting.notification.ms-teams-channels";
    private final static String NOTIFICATION_EMAILS_PROPERTY = "reporting.notification.emails";

    private final static String MILESTONE_ID_PROPERTY = "reporting.milestone.id";
    private final static String MILESTONE_NAME_PROPERTY = "reporting.milestone.name";

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
        String runContext = agentProperties.getProperty(RUN_CONTEXT_PROPERTY);
        Boolean runRetryKnownIssues = parseBoolean(System.getenv(RUN_RETRY_KNOWN_ISSUES_PROPERTY));
        Boolean substituteRemoteWebDrivers = parseBoolean(System.getenv(RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_PROPERTY));
        String slackChannels = agentProperties.getProperty(NOTIFICATION_SLACK_CHANNELS_PROPERTY);
        String msTeamsChannels = agentProperties.getProperty(NOTIFICATION_MS_TEAMS_PROPERTY);
        String emails = agentProperties.getProperty(NOTIFICATION_EMAILS_PROPERTY);
        Long milestoneId = ConfigurationUtils.parseLong(System.getenv(MILESTONE_ID_PROPERTY));
        String milestoneName = agentProperties.getProperty(MILESTONE_NAME_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("Properties configuration is malformed");
        }

        return ReportingConfiguration.builder()
                                     .reportingEnabled(ConfigurationUtils.parseBoolean(enabled))
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(
                                             hostname, accessToken
                                     ))
                                     .run(new ReportingConfiguration.RunConfiguration(
                                             displayName, build, environment, runContext, runRetryKnownIssues, substituteRemoteWebDrivers
                                     ))
                                     .milestone(new ReportingConfiguration.MilestoneConfiguration(
                                             milestoneId, milestoneName
                                     ))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             slackChannels, msTeamsChannels, emails
                                     ))
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

}
