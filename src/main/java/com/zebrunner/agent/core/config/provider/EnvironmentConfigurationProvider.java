package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

import static com.zebrunner.agent.core.config.ConfigurationUtils.parseBoolean;

public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_VARIABLE = "REPORTING_ENABLED";
    private final static String PROJECT_KEY_VARIABLE = "REPORTING_PROJECT_KEY";

    private final static String HOSTNAME_VARIABLE = "REPORTING_SERVER_HOSTNAME";
    private final static String ACCESS_TOKEN_VARIABLE = "REPORTING_SERVER_ACCESS_TOKEN";

    private final static String RUN_DISPLAY_NAME_VARIABLE = "REPORTING_RUN_DISPLAY_NAME";
    private final static String RUN_BUILD_VARIABLE = "REPORTING_RUN_BUILD";
    private final static String RUN_ENVIRONMENT_VARIABLE = "REPORTING_RUN_ENVIRONMENT";
    private final static String RUN_CONTEXT_VARIABLE = "REPORTING_RUN_CONTEXT";
    private final static String RUN_RETRY_KNOWN_ISSUES_VARIABLE = "REPORTING_RUN_RETRY_KNOWN_ISSUES";
    private final static String RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_VARIABLE = "REPORTING_RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS";

    private final static String NOTIFICATION_SLACK_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_SLACK_CHANNELS";
    private final static String NOTIFICATION_MS_TEAMS_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_MS_TEAMS_CHANNELS";
    private final static String NOTIFICATION_EMAILS = "REPORTING_NOTIFICATION_EMAILS";

    private final static String MILESTONE_ID_VARIABLE = "REPORTING_MILESTONE_ID";
    private final static String MILESTONE_NAME_VARIABLE = "REPORTING_MILESTONE_NAME";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getenv(ENABLED_VARIABLE);
        String projectKey = System.getenv(PROJECT_KEY_VARIABLE);
        String hostname = System.getenv(HOSTNAME_VARIABLE);
        String accessToken = System.getenv(ACCESS_TOKEN_VARIABLE);
        String displayName = System.getenv(RUN_DISPLAY_NAME_VARIABLE);
        String build = System.getenv(RUN_BUILD_VARIABLE);
        String environment = System.getenv(RUN_ENVIRONMENT_VARIABLE);
        String runContext = System.getenv(RUN_CONTEXT_VARIABLE);
        Boolean runRetryKnownIssues = parseBoolean(System.getenv(RUN_RETRY_KNOWN_ISSUES_VARIABLE));
        Boolean substituteRemoteWebDrivers = parseBoolean(System.getenv(RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_VARIABLE));
        String slackChannels = System.getenv(NOTIFICATION_SLACK_CHANNELS_VARIABLE);
        String msTeamsChannels = System.getenv(NOTIFICATION_MS_TEAMS_CHANNELS_VARIABLE);
        String emails = System.getenv(NOTIFICATION_EMAILS);
        Long milestoneId = ConfigurationUtils.parseLong(System.getenv(MILESTONE_ID_VARIABLE));
        String milestoneName = System.getenv(MILESTONE_NAME_VARIABLE);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("Environment configuration is malformed");
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

}
