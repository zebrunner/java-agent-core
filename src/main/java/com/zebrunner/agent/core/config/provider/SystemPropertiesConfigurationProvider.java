package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

public class SystemPropertiesConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String PROJECT_KEY_PROPERTY = "reporting.projectKey";

    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.accessToken";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "reporting.run.displayName";
    private final static String RUN_BUILD_PROPERTY = "reporting.run.build";
    private final static String RUN_ENVIRONMENT_PROPERTY = "reporting.run.environment";
    private final static String RUN_CONTEXT_PROPERTY = "reporting.run.context";

    private final static String SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String MS_TEAMS_CHANNELS_PROPERTY = "reporting.notification.ms-teams-channels";
    private final static String EMAILS_PROPERTY = "reporting.notification.emails";

    private final static String MILESTONE_ID_PROPERTY = "reporting.milestone.id";
    private final static String MILESTONE_NAME_PROPERTY = "reporting.milestone.name";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getProperty(ENABLED_PROPERTY);
        String projectKey = System.getProperty(PROJECT_KEY_PROPERTY);
        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        String accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY);
        String displayName = System.getProperty(RUN_DISPLAY_NAME_PROPERTY);
        String build = System.getProperty(RUN_BUILD_PROPERTY);
        String environment = System.getProperty(RUN_ENVIRONMENT_PROPERTY);
        String runContext = System.getProperty(RUN_CONTEXT_PROPERTY);
        String slackChannels = System.getProperty(SLACK_CHANNELS_PROPERTY);
        String msTeamsChannels = System.getProperty(MS_TEAMS_CHANNELS_PROPERTY);
        String emails = System.getProperty(EMAILS_PROPERTY);
        String milestoneId = System.getProperty(MILESTONE_ID_PROPERTY);
        String milestoneName = System.getProperty(MILESTONE_NAME_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("System properties configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .reportingEnabled(reportingEnabled)
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .run(new ReportingConfiguration.RunConfiguration(displayName, build, environment, runContext))
                                     .milestone(new ReportingConfiguration.MilestoneConfiguration(parseLong(milestoneId), milestoneName))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(slackChannels, msTeamsChannels, emails))
                                     .build();
    }

    private Long parseLong(String property) {
        try {
            return Long.valueOf(property);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
