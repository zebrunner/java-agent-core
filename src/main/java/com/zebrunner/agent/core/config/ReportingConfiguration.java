package com.zebrunner.agent.core.config;

import lombok.Data;
import lombok.experimental.Accessors;

import com.zebrunner.agent.core.config.annotation.Configuration;
import com.zebrunner.agent.core.config.annotation.EnvironmentVariable;
import com.zebrunner.agent.core.config.annotation.PropertiesFileProperty;
import com.zebrunner.agent.core.config.annotation.SystemProperty;
import com.zebrunner.agent.core.config.annotation.YamlProperty;

@Data
@Accessors(chain = true)
public class ReportingConfiguration {

    @YamlProperty("reporting.enabled")
    @SystemProperty("reporting.enabled")
    @EnvironmentVariable("REPORTING_ENABLED")
    @PropertiesFileProperty("reporting.enabled")
    private Boolean reportingEnabled;

    @EnvironmentVariable("REPORTING_PROJECT_KEY")
    @YamlProperty({"reporting.project-key", "reporting.projectKey"})
    @SystemProperty({"reporting.project-key", "reporting.projectKey"})
    @PropertiesFileProperty({"reporting.project-key", "reporting.projectKey"})
    private String projectKey;

    @Configuration
    private ServerConfiguration server;

    @Configuration
    private RunConfiguration run;

    @Configuration
    private MilestoneConfiguration milestone;

    @Configuration
    private NotificationConfiguration notification;

    @Configuration
    private TcmConfiguration tcm;

    public boolean isReportingEnabled() {
        return reportingEnabled != null && reportingEnabled;
    }

    @Data
    public static class MilestoneConfiguration {

        @YamlProperty("reporting.milestone.id")
        @SystemProperty("reporting.milestone.id")
        @EnvironmentVariable("REPORTING_MILESTONE_ID")
        @PropertiesFileProperty("reporting.milestone.id")
        private Long id;

        @YamlProperty("reporting.milestone.name")
        @SystemProperty("reporting.milestone.name")
        @EnvironmentVariable("REPORTING_MILESTONE_NAME")
        @PropertiesFileProperty("reporting.milestone.name")
        private String name;

    }

    @Data
    public static class ServerConfiguration {

        @YamlProperty("reporting.server.hostname")
        @SystemProperty("reporting.server.hostname")
        @EnvironmentVariable("REPORTING_SERVER_HOSTNAME")
        @PropertiesFileProperty("reporting.server.hostname")
        private String hostname;

        @YamlProperty("reporting.server.access-token")
        @EnvironmentVariable("REPORTING_SERVER_ACCESS_TOKEN")
        @PropertiesFileProperty("reporting.server.access-token")
        @SystemProperty({"reporting.server.access-token", "reporting.server.accessToken"})
        private String accessToken;

    }

    @Data
    public static class RunConfiguration {

        @EnvironmentVariable("REPORTING_RUN_DISPLAY_NAME")
        @YamlProperty({"reporting.run.display-name", "reporting.run.displayName"})
        @SystemProperty({"reporting.run.display-name", "reporting.run.displayName"})
        @PropertiesFileProperty({"reporting.run.display-name", "reporting.run.displayName"})
        private String displayName;

        @YamlProperty("reporting.run.build")
        @SystemProperty("reporting.run.build")
        @EnvironmentVariable("REPORTING_RUN_BUILD")
        @PropertiesFileProperty("reporting.run.build")
        private String build;

        @YamlProperty("reporting.run.environment")
        @SystemProperty("reporting.run.environment")
        @EnvironmentVariable("REPORTING_RUN_ENVIRONMENT")
        @PropertiesFileProperty("reporting.run.environment")
        private String environment;

        @YamlProperty("reporting.run.context")
        @SystemProperty("reporting.run.context")
        @EnvironmentVariable("REPORTING_RUN_CONTEXT")
        @PropertiesFileProperty("reporting.run.context")
        private String context;

        @YamlProperty("reporting.run.retry-known-issues")
        @EnvironmentVariable("REPORTING_RUN_RETRY_KNOWN_ISSUES")
        @PropertiesFileProperty("reporting.run.retry-known-issues")
        @SystemProperty({"reporting.run.retry-known-issues", "reporting.run.retryKnownIssues"})
        private Boolean retryKnownIssues;

        @EnvironmentVariable("REPORTING_RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS")
        private Boolean substituteRemoteWebDrivers;

        @YamlProperty("reporting.run.treat-skips-as-failures")
        @SystemProperty("reporting.run.treat-skips-as-failures")
        @EnvironmentVariable("REPORTING_RUN_TREAT_SKIPS_AS_FAILURES")
        @PropertiesFileProperty("reporting.run.treat-skips-as-failures")
        private Boolean treatSkipsAsFailures;

    }

    @Data
    public static class NotificationConfiguration {

        @YamlProperty("reporting.notification.enabled")
        @SystemProperty("reporting.notification.enabled")
        @EnvironmentVariable("REPORTING_NOTIFICATION_ENABLED")
        @PropertiesFileProperty("reporting.notification.enabled")
        private Boolean enabled;

        @YamlProperty("reporting.notification.notify-on-each-failure")
        @SystemProperty("reporting.notification.notify-on-each-failure")
        @EnvironmentVariable("REPORTING_NOTIFICATION_NOTIFY_ON_EACH_FAILURE")
        @PropertiesFileProperty("reporting.notification.notify-on-each-failure")
        private Boolean notifyOnEachFailure;

        @YamlProperty("reporting.notification.slack-channels")
        @SystemProperty("reporting.notification.slack-channels")
        @EnvironmentVariable("REPORTING_NOTIFICATION_SLACK_CHANNELS")
        @PropertiesFileProperty("reporting.notification.slack-channels")
        private String slackChannels;

        @YamlProperty("reporting.notification.ms-teams-channels")
        @SystemProperty("reporting.notification.ms-teams-channels")
        @EnvironmentVariable("REPORTING_NOTIFICATION_MS_TEAMS_CHANNELS")
        @PropertiesFileProperty("reporting.notification.ms-teams-channels")
        private String msTeamsChannels;

        @YamlProperty("reporting.notification.emails")
        @SystemProperty("reporting.notification.emails")
        @EnvironmentVariable("REPORTING_NOTIFICATION_EMAILS")
        @PropertiesFileProperty("reporting.notification.emails")
        private String emails;

    }

    @Data
    public static class TcmConfiguration {

        @Configuration
        private TestCaseStatus testCaseStatus = new TestCaseStatus();
        @Configuration
        private Zebrunner zebrunner = new Zebrunner();
        @Configuration
        private TestRail testRail = new TestRail();
        @Configuration
        private Xray xray = new Xray();
        @Configuration
        private Zephyr zephyr = new Zephyr();

        @Data
        public static class TestCaseStatus {

            @YamlProperty("reporting.tcm.test-case-status.on-pass")
            @SystemProperty("reporting.tcm.test-case-status.on-pass")
            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_PASS")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-pass")
            private String onPass;

            @YamlProperty("reporting.tcm.test-case-status.on-fail")
            @SystemProperty("reporting.tcm.test-case-status.on-fail")
            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_FAIL")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-fail")
            private String onFail;

            @YamlProperty("reporting.tcm.test-case-status.on-known-issue")
            @SystemProperty("reporting.tcm.test-case-status.on-known-issue")
            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_KNOWN_ISSUE")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-known-issue")
            private String onKnownIssue;

            @YamlProperty("reporting.tcm.test-case-status.on-skip")
            @SystemProperty("reporting.tcm.test-case-status.on-skip")
            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_SKIP")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-skip")
            private String onSkip;

            @YamlProperty("reporting.tcm.test-case-status.on-block")
            @SystemProperty("reporting.tcm.test-case-status.on-block")
            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_BLOCK")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-block")
            private String onBlock;

            public boolean hasAnySpecified() {
                return onPass != null
                       || onFail != null
                       || onKnownIssue != null
                       || onSkip != null
                       || onBlock != null;
            }

        }

        @Data
        public static class Zebrunner {

            @YamlProperty("reporting.tcm.zebrunner.push-results")
            @SystemProperty("reporting.tcm.zebrunner.push-results")
            @EnvironmentVariable("REPORTING_TCM_ZEBRUNNER_PUSH_RESULTS")
            @PropertiesFileProperty("reporting.tcm.zebrunner.push-results")
            private Boolean pushResults;

            @YamlProperty("reporting.tcm.zebrunner.push-in-real-time")
            @SystemProperty("reporting.tcm.zebrunner.push-in-real-time")
            @EnvironmentVariable("REPORTING_TCM_ZEBRUNNER_PUSH_IN_REAL_TIME")
            @PropertiesFileProperty("reporting.tcm.zebrunner.push-in-real-time")
            private Boolean pushInRealTime;

            @EnvironmentVariable("REPORTING_TCM_ZEBRUNNER_RUN_ID")
            @YamlProperty({"reporting.tcm.zebrunner.test-run-id", "reporting.tcm.zebrunner.run-id"})
            @SystemProperty({"reporting.tcm.zebrunner.test-run-id", "reporting.tcm.zebrunner.run-id"})
            @PropertiesFileProperty({"reporting.tcm.zebrunner.test-run-id", "reporting.tcm.zebrunner.run-id"})
            private String testRunId;

        }

        @Data
        public static class TestRail {

            @YamlProperty("reporting.tcm.test-rail.push-results")
            @SystemProperty("reporting.tcm.test-rail.push-results")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_PUSH_RESULTS")
            @PropertiesFileProperty("reporting.tcm.test-rail.push-results")
            private Boolean pushResults;

            @YamlProperty("reporting.tcm.test-rail.push-in-real-time")
            @SystemProperty("reporting.tcm.test-rail.push-in-real-time")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_PUSH_IN_REAL_TIME")
            @PropertiesFileProperty("reporting.tcm.test-rail.push-in-real-time")
            private Boolean pushInRealTime;

            @YamlProperty("reporting.tcm.test-rail.suite-id")
            @SystemProperty("reporting.tcm.test-rail.suite-id")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_SUITE_ID")
            @PropertiesFileProperty("reporting.tcm.test-rail.suite-id")
            private String suiteId;

            @YamlProperty("reporting.tcm.test-rail.run-id")
            @SystemProperty("reporting.tcm.test-rail.run-id")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_RUN_ID")
            @PropertiesFileProperty("reporting.tcm.test-rail.run-id")
            private String runId;

            @YamlProperty("reporting.tcm.test-rail.include-all-test-cases-in-new-run")
            @SystemProperty("reporting.tcm.test-rail.include-all-test-cases-in-new-run")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_INCLUDE_ALL_TEST_CASES_IN_NEW_RUN")
            @PropertiesFileProperty("reporting.tcm.test-rail.include-all-test-cases-in-new-run")
            private Boolean includeAllTestCasesInNewRun;

            @YamlProperty("reporting.tcm.test-rail.run-name")
            @SystemProperty("reporting.tcm.test-rail.run-name")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_RUN_NAME")
            @PropertiesFileProperty("reporting.tcm.test-rail.run-name")
            private String runName;

            @YamlProperty("reporting.tcm.test-rail.milestone-name")
            @SystemProperty("reporting.tcm.test-rail.milestone-name")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_MILESTONE_NAME")
            @PropertiesFileProperty("reporting.tcm.test-rail.milestone-name")
            private String milestoneName;

            @YamlProperty("reporting.tcm.test-rail.assignee")
            @SystemProperty("reporting.tcm.test-rail.assignee")
            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_ASSIGNEE")
            @PropertiesFileProperty("reporting.tcm.test-rail.assignee")
            private String assignee;

        }

        @Data
        public static class Xray {

            @YamlProperty("reporting.tcm.xray.push-results")
            @SystemProperty("reporting.tcm.xray.push-results")
            @EnvironmentVariable("REPORTING_TCM_XRAY_PUSH_RESULTS")
            @PropertiesFileProperty("reporting.tcm.xray.push-results")
            private Boolean pushResults;

            @YamlProperty("reporting.tcm.xray.push-in-real-time")
            @SystemProperty("reporting.tcm.xray.push-in-real-time")
            @EnvironmentVariable("REPORTING_TCM_XRAY_PUSH_IN_REAL_TIME")
            @PropertiesFileProperty("reporting.tcm.xray.push-in-real-time")
            private Boolean pushInRealTime;

            @YamlProperty("reporting.tcm.xray.execution-key")
            @SystemProperty("reporting.tcm.xray.execution-key")
            @EnvironmentVariable("REPORTING_TCM_XRAY_EXECUTION_KEY")
            @PropertiesFileProperty("reporting.tcm.xray.execution-key")
            private String executionKey;

        }

        @Data
        public static class Zephyr {

            @YamlProperty("reporting.tcm.zephyr.push-results")
            @SystemProperty("reporting.tcm.zephyr.push-results")
            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_PUSH_RESULTS")
            @PropertiesFileProperty("reporting.tcm.zephyr.push-results")
            private Boolean pushResults;

            @YamlProperty("reporting.tcm.zephyr.push-in-real-time")
            @SystemProperty("reporting.tcm.zephyr.push-in-real-time")
            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_PUSH_IN_REAL_TIME")
            @PropertiesFileProperty("reporting.tcm.zephyr.push-in-real-time")
            private Boolean pushInRealTime;

            @YamlProperty("reporting.tcm.zephyr.jira-project-key")
            @SystemProperty("reporting.tcm.zephyr.jira-project-key")
            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_JIRA_PROJECT_KEY")
            @PropertiesFileProperty("reporting.tcm.zephyr.jira-project-key")
            private String jiraProjectKey;

            @YamlProperty("reporting.tcm.zephyr.test-cycle-key")
            @SystemProperty("reporting.tcm.zephyr.test-cycle-key")
            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_TEST_CYCLE_KEY")
            @PropertiesFileProperty("reporting.tcm.zephyr.test-cycle-key")
            private String testCycleKey;

        }

    }

}
