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

    private final static String RERUN_CONDITION_PROPERTY = "reporting.rerunCondition";

    private final static String SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String MS_TEAMS_CHANNELS_PROPERTY = "reporting.notification.ms-teams-channels";
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
        String rerunCondition = System.getProperty(RERUN_CONDITION_PROPERTY);
        String slackChannels = System.getProperty(SLACK_CHANNELS_PROPERTY);
        String msTeamsChannels = System.getProperty(MS_TEAMS_CHANNELS_PROPERTY);
        String emails = System.getProperty(EMAILS_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("System properties configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .reportingEnabled(reportingEnabled)
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .run(new ReportingConfiguration.RunConfiguration(displayName, build, environment))
                                     .rerunCondition(rerunCondition)
                                     .notification(new ReportingConfiguration.NotificationConfiguration(slackChannels, msTeamsChannels, emails))
                                     .build();
    }

}
