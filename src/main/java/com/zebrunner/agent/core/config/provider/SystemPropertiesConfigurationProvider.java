package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
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
    private final static String RUN_RETRY_KNOWN_ISSUES_PROPERTY = "reporting.run.retryKnownIssues";
    private final static String RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_PROPERTY = "reporting.run.substituteRemoteWebDrivers";
    private final static String RUN_TREAT_SKIPS_AS_FAILURES_PROPERTY = "reporting.run.treat-skips-as-failures";
    private final static String RUN_TEST_CASE_STATUS_ON_PASS_PROPERTY = "reporting.run.test-case-status.on-pass";
    private final static String RUN_TEST_CASE_STATUS_ON_FAIL_PROPERTY = "reporting.run.test-case-status.on-fail";
    private final static String RUN_TEST_CASE_STATUS_ON_SKIP_PROPERTY = "reporting.run.test-case-status.on-skip";

    private static final String NOTIFICATION_NOTIFY_ON_EACH_FAILURE_VARIABLE = "reporting.notification.notify-on-each-failure";
    private final static String SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String MS_TEAMS_CHANNELS_PROPERTY = "reporting.notification.ms-teams-channels";
    private final static String EMAILS_PROPERTY = "reporting.notification.emails";

    private final static String MILESTONE_ID_PROPERTY = "reporting.milestone.id";
    private final static String MILESTONE_NAME_PROPERTY = "reporting.milestone.name";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getProperty(ENABLED_PROPERTY);

        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        String accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY);
        String projectKey = System.getProperty(PROJECT_KEY_PROPERTY);

        String displayName = System.getProperty(RUN_DISPLAY_NAME_PROPERTY);
        String build = System.getProperty(RUN_BUILD_PROPERTY);
        String environment = System.getProperty(RUN_ENVIRONMENT_PROPERTY);
        String runContext = System.getProperty(RUN_CONTEXT_PROPERTY);
        Boolean runRetryKnownIssues = ConfigurationUtils.parseBoolean(System.getProperty(RUN_RETRY_KNOWN_ISSUES_PROPERTY));
        Boolean substituteRemoteWebDrivers = ConfigurationUtils.parseBoolean(System.getProperty(RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_PROPERTY));
        Boolean treatSkipsAsFailures = ConfigurationUtils.parseBoolean(System.getProperty(RUN_TREAT_SKIPS_AS_FAILURES_PROPERTY));
        String testCaseStatusOnPass = System.getProperty(RUN_TEST_CASE_STATUS_ON_PASS_PROPERTY);
        String testCaseStatusOnFail = System.getProperty(RUN_TEST_CASE_STATUS_ON_FAIL_PROPERTY);
        String testCaseStatusOnSkip = System.getProperty(RUN_TEST_CASE_STATUS_ON_SKIP_PROPERTY);

        Boolean notifyOnEachFailure = ConfigurationUtils.parseBoolean(System.getProperty(NOTIFICATION_NOTIFY_ON_EACH_FAILURE_VARIABLE));
        String slackChannels = System.getProperty(SLACK_CHANNELS_PROPERTY);
        String msTeamsChannels = System.getProperty(MS_TEAMS_CHANNELS_PROPERTY);
        String emails = System.getProperty(EMAILS_PROPERTY);

        Long milestoneId = ConfigurationUtils.parseLong(System.getProperty(MILESTONE_ID_PROPERTY));
        String milestoneName = System.getProperty(MILESTONE_NAME_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("System properties configuration is malformed");
        }

        return ReportingConfiguration.builder()
                                     .reportingEnabled(ConfigurationUtils.parseBoolean(enabled))
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(
                                             hostname, accessToken
                                     ))
                                     .run(new ReportingConfiguration.RunConfiguration(
                                             displayName, build, environment, runContext, runRetryKnownIssues,
                                             substituteRemoteWebDrivers, treatSkipsAsFailures,
                                             new ReportingConfiguration.RunConfiguration.TestCaseStatus(
                                                     testCaseStatusOnPass, testCaseStatusOnFail, testCaseStatusOnSkip
                                             )
                                     ))
                                     .milestone(new ReportingConfiguration.MilestoneConfiguration(
                                             milestoneId, milestoneName
                                     ))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             notifyOnEachFailure, slackChannels, msTeamsChannels, emails
                                     ))
                                     .build();
    }

}
