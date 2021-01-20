package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_VARIABLE = "REPORTING_ENABLED";
    private final static String PROJECT_KEY_VARIABLE = "REPORTING_PROJECT_KEY";

    private final static String HOSTNAME_VARIABLE = "REPORTING_SERVER_HOSTNAME";
    private final static String ACCESS_TOKEN_VARIABLE = "REPORTING_SERVER_ACCESS_TOKEN";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "REPORTING_RUN_DISPLAY_NAME";
    private final static String RUN_BUILD_PROPERTY = "REPORTING_RUN_BUILD";
    private final static String RUN_ENVIRONMENT_PROPERTY = "REPORTING_RUN_ENVIRONMENT";

    private final static String RUN_ID_VARIABLE = "REPORTING_RERUN_RUN_ID";

    private final static String NOTIFICATION_SLACK_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_SLACK_CHANNELS";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getenv(ENABLED_VARIABLE);
        String projectKey = System.getenv(PROJECT_KEY_VARIABLE);
        String hostname = System.getenv(HOSTNAME_VARIABLE);
        String accessToken = System.getenv(ACCESS_TOKEN_VARIABLE);
        String displayName = System.getenv(RUN_DISPLAY_NAME_PROPERTY);
        String build = System.getenv(RUN_BUILD_PROPERTY);
        String environment = System.getenv(RUN_ENVIRONMENT_PROPERTY);
        String runId = System.getenv(RUN_ID_VARIABLE);
        String slackChannels = System.getenv(NOTIFICATION_SLACK_CHANNELS_VARIABLE);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("Environment configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .reportingEnabled(reportingEnabled)
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .run(new ReportingConfiguration.RunConfiguration(displayName, build, environment))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             new ReportingConfiguration.NotificationConfiguration.Slack(slackChannels)
                                     ))
                                     .build();
    }

}
