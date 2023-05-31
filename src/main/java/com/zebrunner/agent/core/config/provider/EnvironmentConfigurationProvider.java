package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

import static com.zebrunner.agent.core.config.ConfigurationUtils.parseBoolean;

public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    private static final String ENABLED_VARIABLE = "REPORTING_ENABLED";
    private static final String PROJECT_KEY_VARIABLE = "REPORTING_PROJECT_KEY";

    private static final String HOSTNAME_VARIABLE = "REPORTING_SERVER_HOSTNAME";
    private static final String ACCESS_TOKEN_VARIABLE = "REPORTING_SERVER_ACCESS_TOKEN";

    private static final String RUN_DISPLAY_NAME_VARIABLE = "REPORTING_RUN_DISPLAY_NAME";
    private static final String RUN_BUILD_VARIABLE = "REPORTING_RUN_BUILD";
    private static final String RUN_ENVIRONMENT_VARIABLE = "REPORTING_RUN_ENVIRONMENT";
    private static final String RUN_CONTEXT_VARIABLE = "REPORTING_RUN_CONTEXT";
    private static final String RUN_RETRY_KNOWN_ISSUES_VARIABLE = "REPORTING_RUN_RETRY_KNOWN_ISSUES";
    private static final String RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_VARIABLE = "REPORTING_RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS";
    private static final String RUN_TREAT_SKIPS_AS_FAILURES_VARIABLE = "REPORTING_RUN_TREAT_SKIPS_AS_FAILURES";

    private static final String TCM_TEST_CASE_STATUS_ON_PASS_VARIABLE = "REPORTING_TCM_TEST_CASE_STATUS_ON_PASS";
    private static final String TCM_TEST_CASE_STATUS_ON_FAIL_VARIABLE = "REPORTING_TCM_TEST_CASE_STATUS_ON_FAIL";
    private static final String TCM_TEST_CASE_STATUS_ON_SKIP_VARIABLE = "REPORTING_TCM_TEST_CASE_STATUS_ON_SKIP";

    private static final String TCM_ZEBRUNNER_PUSH_RESULTS_VARIABLE = "REPORTING_TCM_ZEBRUNNER_PUSH_RESULTS";
    private static final String TCM_ZEBRUNNER_PUSH_IN_REAL_TIME_VARIABLE = "REPORTING_TCM_ZEBRUNNER_PUSH_IN_REAL_TIME";
    private static final String TCM_ZEBRUNNER_TEST_RUN_ID_VARIABLE = "REPORTING_TCM_ZEBRUNNER_RUN_ID";

    private static final String TCM_TEST_RAIL_PUSH_RESULTS_VARIABLE = "REPORTING_TCM_TEST_RAIL_PUSH_RESULTS";
    private static final String TCM_TEST_RAIL_PUSH_IN_REAL_TIME_VARIABLE = "REPORTING_TCM_TEST_RAIL_PUSH_IN_REAL_TIME";
    private static final String TCM_TEST_RAIL_SUITE_ID_VARIABLE = "REPORTING_TCM_TEST_RAIL_SUITE_ID";
    private static final String TCM_TEST_RAIL_RUN_ID_VARIABLE = "REPORTING_TCM_TEST_RAIL_RUN_ID";
    private static final String TCM_TEST_RAIL_INCLUDE_ALL_TEST_CASES_IN_NEW_RUN_VARIABLE = "REPORTING_TCM_TEST_RAIL_INCLUDE_ALL_TEST_CASES_IN_NEW_RUN";
    private static final String TCM_TEST_RAIL_RUN_NAME_VARIABLE = "REPORTING_TCM_TEST_RAIL_RUN_NAME";
    private static final String TCM_TEST_RAIL_MILESTONE_NAME_VARIABLE = "REPORTING_TCM_TEST_RAIL_MILESTONE_NAME";
    private static final String TCM_TEST_RAIL_ASSIGNEE_VARIABLE = "REPORTING_TCM_TEST_RAIL_ASSIGNEE";

    private static final String TCM_XRAY_PUSH_RESULTS_VARIABLE = "REPORTING_TCM_XRAY_PUSH_RESULTS";
    private static final String TCM_XRAY_PUSH_IN_REAL_TIME_VARIABLE = "REPORTING_TCM_XRAY_PUSH_IN_REAL_TIME";
    private static final String TCM_XRAY_EXECUTION_KEY_VARIABLE = "REPORTING_TCM_XRAY_EXECUTION_KEY";

    private static final String TCM_ZEPHYR_PUSH_RESULTS_VARIABLE = "REPORTING_TCM_ZEPHYR_PUSH_RESULTS";
    private static final String TCM_ZEPHYR_PUSH_IN_REAL_TIME_VARIABLE = "REPORTING_TCM_ZEPHYR_PUSH_IN_REAL_TIME";
    private static final String TCM_ZEPHYR_JIRA_PROJECT_KEY_VARIABLE = "REPORTING_TCM_ZEPHYR_JIRA_PROJECT_KEY";
    private static final String TCM_ZEPHYR_TEST_CYCLE_KEY_VARIABLE = "REPORTING_TCM_ZEPHYR_TEST_CYCLE_KEY";

    private static final String NOTIFICATION_ENABLED = "REPORTING_NOTIFICATION_ENABLED";
    private static final String NOTIFICATION_NOTIFY_ON_EACH_FAILURE_VARIABLE = "REPORTING_NOTIFICATION_NOTIFY_ON_EACH_FAILURE";
    private static final String NOTIFICATION_SLACK_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_SLACK_CHANNELS";
    private static final String NOTIFICATION_MS_TEAMS_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_MS_TEAMS_CHANNELS";
    private static final String NOTIFICATION_EMAILS = "REPORTING_NOTIFICATION_EMAILS";

    private static final String MILESTONE_ID_VARIABLE = "REPORTING_MILESTONE_ID";
    private static final String MILESTONE_NAME_VARIABLE = "REPORTING_MILESTONE_NAME";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getenv(ENABLED_VARIABLE);

        String hostname = System.getenv(HOSTNAME_VARIABLE);
        String accessToken = System.getenv(ACCESS_TOKEN_VARIABLE);
        String projectKey = System.getenv(PROJECT_KEY_VARIABLE);

        String displayName = System.getenv(RUN_DISPLAY_NAME_VARIABLE);
        String build = System.getenv(RUN_BUILD_VARIABLE);
        String environment = System.getenv(RUN_ENVIRONMENT_VARIABLE);
        String runContext = System.getenv(RUN_CONTEXT_VARIABLE);
        Boolean runRetryKnownIssues = parseBoolean(System.getenv(RUN_RETRY_KNOWN_ISSUES_VARIABLE));
        Boolean substituteRemoteWebDrivers = parseBoolean(System.getenv(RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_VARIABLE));
        Boolean treatSkipsAsFailures = parseBoolean(System.getenv(RUN_TREAT_SKIPS_AS_FAILURES_VARIABLE));
        String testCaseStatusOnPass = System.getenv(TCM_TEST_CASE_STATUS_ON_PASS_VARIABLE);
        String testCaseStatusOnFail = System.getenv(TCM_TEST_CASE_STATUS_ON_FAIL_VARIABLE);
        String testCaseStatusOnSkip = System.getenv(TCM_TEST_CASE_STATUS_ON_SKIP_VARIABLE);

        Boolean tcmPushResults = ConfigurationUtils.parseBoolean(System.getenv(TCM_ZEBRUNNER_PUSH_RESULTS_VARIABLE));
        Boolean tcmPushInRealTime = ConfigurationUtils.parseBoolean(System.getenv(TCM_ZEBRUNNER_PUSH_IN_REAL_TIME_VARIABLE));
        String tcmTestRunId = System.getenv(TCM_ZEBRUNNER_TEST_RUN_ID_VARIABLE);

        Boolean testRailPushResults = ConfigurationUtils.parseBoolean(System.getenv(TCM_TEST_RAIL_PUSH_RESULTS_VARIABLE));
        Boolean testRailPushInRealTime = ConfigurationUtils.parseBoolean(System.getenv(TCM_TEST_RAIL_PUSH_IN_REAL_TIME_VARIABLE));
        String testRailSuiteId = System.getenv(TCM_TEST_RAIL_SUITE_ID_VARIABLE);
        String testRailRunId = System.getenv(TCM_TEST_RAIL_RUN_ID_VARIABLE);
        Boolean testRailIncludeAllTestCasesInNewRun = ConfigurationUtils.parseBoolean(System.getenv(TCM_TEST_RAIL_INCLUDE_ALL_TEST_CASES_IN_NEW_RUN_VARIABLE));
        String testRailRunName = System.getenv(TCM_TEST_RAIL_RUN_NAME_VARIABLE);
        String testRailMilestoneName = System.getenv(TCM_TEST_RAIL_MILESTONE_NAME_VARIABLE);
        String testRailAssignee = System.getenv(TCM_TEST_RAIL_ASSIGNEE_VARIABLE);

        Boolean xrayPushResults = ConfigurationUtils.parseBoolean(System.getenv(TCM_XRAY_PUSH_RESULTS_VARIABLE));
        Boolean xrayPushInRealTime = ConfigurationUtils.parseBoolean(System.getenv(TCM_XRAY_PUSH_IN_REAL_TIME_VARIABLE));
        String xrayExecutionKey = System.getenv(TCM_XRAY_EXECUTION_KEY_VARIABLE);

        Boolean zephyrPushResults = ConfigurationUtils.parseBoolean(System.getenv(TCM_ZEPHYR_PUSH_RESULTS_VARIABLE));
        Boolean zephyrPushInRealTime = ConfigurationUtils.parseBoolean(System.getenv(TCM_ZEPHYR_PUSH_IN_REAL_TIME_VARIABLE));
        String zephyrJiraProjectKey = System.getenv(TCM_ZEPHYR_JIRA_PROJECT_KEY_VARIABLE);
        String zephyrTestCycleKey = System.getenv(TCM_ZEPHYR_TEST_CYCLE_KEY_VARIABLE);

        Boolean notificationsEnabled = parseBoolean(System.getenv(NOTIFICATION_ENABLED));
        Boolean notifyOnEachFailure = parseBoolean(System.getenv(NOTIFICATION_NOTIFY_ON_EACH_FAILURE_VARIABLE));
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
                                             displayName, build, environment, runContext, runRetryKnownIssues,
                                             substituteRemoteWebDrivers, treatSkipsAsFailures
                                     ))
                                     .milestone(new ReportingConfiguration.MilestoneConfiguration(
                                             milestoneId, milestoneName
                                     ))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             notificationsEnabled, notifyOnEachFailure, slackChannels, msTeamsChannels, emails
                                     ))
                                     .tcm(new ReportingConfiguration.TcmConfiguration(
                                             new ReportingConfiguration.TcmConfiguration.TestCaseStatus(
                                                     testCaseStatusOnPass, testCaseStatusOnFail, testCaseStatusOnSkip
                                             ),
                                             new ReportingConfiguration.TcmConfiguration.Zebrunner(
                                                     tcmPushResults, tcmPushInRealTime, tcmTestRunId
                                             ),
                                             ReportingConfiguration.TcmConfiguration.TestRail.builder()
                                                                                             .pushResults(testRailPushResults)
                                                                                             .pushInRealTime(testRailPushInRealTime)
                                                                                             .suiteId(testRailSuiteId)
                                                                                             .runId(testRailRunId)
                                                                                             .includeAllTestCasesInNewRun(testRailIncludeAllTestCasesInNewRun)
                                                                                             .runName(testRailRunName)
                                                                                             .milestoneName(testRailMilestoneName)
                                                                                             .assignee(testRailAssignee)
                                                                                             .build(),
                                             new ReportingConfiguration.TcmConfiguration.Xray(xrayPushResults, xrayPushInRealTime, xrayExecutionKey),
                                             new ReportingConfiguration.TcmConfiguration.Zephyr(zephyrPushResults, zephyrPushInRealTime, zephyrJiraProjectKey, zephyrTestCycleKey)
                                     ))
                                     .build();
    }

}
