package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SystemPropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String PROPERTY_SEPARATORS = "[,;]";

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String PROJECT_KEY_PROPERTY = "reporting.projectKey";

    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.accessToken";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "reporting.run.displayName";
    private final static String RUN_BUILD_PROPERTY = "reporting.run.build";
    private final static String RUN_ENVIRONMENT_PROPERTY = "reporting.run.environment";

    private final static String RUN_ID_PROPERTY = "reporting.rerun.runId";

    private final static String SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String MICROSOFT_TEAMS_CHANNELS_PROPERTY = "reporting.notification.microsoft-teams-channels";
    private final static String EMAILS_PROPERTY = "reporting.notification.emails";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getProperty(ENABLED_PROPERTY);
        String projectKey = System.getProperty(PROJECT_KEY_PROPERTY);
        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        String accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY);
        String displayName = System.getProperty(RUN_DISPLAY_NAME_PROPERTY);
        String build = System.getProperty(RUN_BUILD_PROPERTY);
        String environment = System.getProperty(RUN_ENVIRONMENT_PROPERTY);
        String runId = System.getProperty(RUN_ID_PROPERTY);
        Set<String> slackChannels = getPropertyValueAsSet(SLACK_CHANNELS_PROPERTY);
        Set<String> microsoftTeamsChannels = getPropertyValueAsSet(MICROSOFT_TEAMS_CHANNELS_PROPERTY);
        Set<String> emails = getPropertyValueAsSet(EMAILS_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("System properties configuration is malformed, skipping");
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

    private Set<String> getPropertyValueAsSet(String key) {
        String propertyListAsString = System.getProperty(key);

        if (propertyListAsString == null) {
            return Set.of();
        }

        return Arrays.stream(propertyListAsString.split(PROPERTY_SEPARATORS))
                     .filter(channel -> !channel.isBlank())
                     .map(String::trim)
                     .collect(Collectors.toSet());
    }

}
