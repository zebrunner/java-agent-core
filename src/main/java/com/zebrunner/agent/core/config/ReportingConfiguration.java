package com.zebrunner.agent.core.config;

import lombok.Data;
import lombok.experimental.Accessors;

import com.zebrunner.agent.core.config.annotation.EnvironmentVariable;
import com.zebrunner.agent.core.config.annotation.InjectConfiguration;
import com.zebrunner.agent.core.config.annotation.PropertiesFileProperty;
import com.zebrunner.agent.core.config.annotation.SystemProperty;
import com.zebrunner.agent.core.config.annotation.YamlProperty;

@Data
@Accessors(chain = true)
public class ReportingConfiguration implements Configuration<ReportingConfiguration> {

    @EnvironmentVariable("REPORTING_ENABLED")
    @SystemProperty("reporting.enabled")
    @YamlProperty("reporting.enabled")
    @PropertiesFileProperty("reporting.enabled")
    private Boolean reportingEnabled;

    @EnvironmentVariable("REPORTING_PROJECT_KEY")
    @SystemProperty({"reporting.project-key", "reporting.projectKey"})
    @YamlProperty({"reporting.project-key", "reporting.projectKey"})
    @PropertiesFileProperty({"reporting.project-key", "reporting.projectKey"})
    private String projectKey;

    @InjectConfiguration
    private Server server = new Server();

    @InjectConfiguration
    private Run run = new Run();

    @InjectConfiguration
    private Milestone milestone = new Milestone();

    @InjectConfiguration
    private Notification notification = new Notification();

    @InjectConfiguration
    private Tcm tcm = new Tcm();

    public boolean isReportingEnabled() {
        return reportingEnabled != null && reportingEnabled;
    }

    @Override
    public void copyMissing(ReportingConfiguration providedConfig) {
        this.setIfNull(this::getReportingEnabled, providedConfig::getReportingEnabled, this::setReportingEnabled);
        this.setIfNull(this::getProjectKey, providedConfig::getProjectKey, this::setProjectKey);
        server.copyMissing(providedConfig.getServer());
        run.copyMissing(providedConfig.getRun());
        milestone.copyMissing(providedConfig.getMilestone());
        notification.copyMissing(providedConfig.getNotification());
        tcm.copyMissing(providedConfig.getTcm());
    }

    @Data
    public static class Server implements Configuration<Server> {

        @EnvironmentVariable("REPORTING_SERVER_HOSTNAME")
        @SystemProperty("reporting.server.hostname")
        @YamlProperty("reporting.server.hostname")
        @PropertiesFileProperty("reporting.server.hostname")
        private String hostname;

        @EnvironmentVariable("REPORTING_SERVER_ACCESS_TOKEN")
        @SystemProperty({"reporting.server.access-token", "reporting.server.accessToken"})
        @YamlProperty("reporting.server.access-token")
        @PropertiesFileProperty("reporting.server.access-token")
        private String accessToken;

        public boolean areAllSet() {
            return hostname != null && accessToken != null;
        }

        @Override
        public void copyMissing(Server providedConfig) {
            this.setIfNull(this::getHostname, providedConfig::getHostname, this::setHostname);
            this.setIfNull(this::getAccessToken, providedConfig::getAccessToken, this::setAccessToken);
        }

    }

    @Data
    public static class Run implements Configuration<Run> {

        @EnvironmentVariable("REPORTING_RUN_DISPLAY_NAME")
        @SystemProperty({"reporting.run.display-name", "reporting.run.displayName"})
        @YamlProperty({"reporting.run.display-name", "reporting.run.displayName"})
        @PropertiesFileProperty({"reporting.run.display-name", "reporting.run.displayName"})
        private String displayName;

        @EnvironmentVariable("REPORTING_RUN_BUILD")
        @SystemProperty("reporting.run.build")
        @YamlProperty("reporting.run.build")
        @PropertiesFileProperty("reporting.run.build")
        private String build;

        @EnvironmentVariable("REPORTING_RUN_LOCALE")
        @SystemProperty({"reporting.run.locale", "locale"})
        @YamlProperty("reporting.run.locale")
        @PropertiesFileProperty("reporting.run.locale")
        private String locale;

        @EnvironmentVariable("REPORTING_RUN_ENVIRONMENT")
        @SystemProperty("reporting.run.environment")
        @YamlProperty("reporting.run.environment")
        @PropertiesFileProperty("reporting.run.environment")
        private String environment;

        @EnvironmentVariable("REPORTING_RUN_CONTEXT")
        private String context;

        @EnvironmentVariable("REPORTING_RUN_RETRY_KNOWN_ISSUES")
        @SystemProperty({"reporting.run.retry-known-issues", "reporting.run.retryKnownIssues"})
        @YamlProperty("reporting.run.retry-known-issues")
        @PropertiesFileProperty("reporting.run.retry-known-issues")
        private Boolean retryKnownIssues;

        @EnvironmentVariable("REPORTING_RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS")
        private Boolean substituteRemoteWebDrivers;

        @EnvironmentVariable("REPORTING_RUN_TREAT_SKIPS_AS_FAILURES")
        @SystemProperty("reporting.run.treat-skips-as-failures")
        @YamlProperty("reporting.run.treat-skips-as-failures")
        @PropertiesFileProperty("reporting.run.treat-skips-as-failures")
        private Boolean treatSkipsAsFailures;

        @Override
        public void copyMissing(Run providedConfig) {
            this.setIfNull(this::getDisplayName, providedConfig::getDisplayName, this::setDisplayName);
            this.setIfNull(this::getBuild, providedConfig::getBuild, this::setBuild);
            this.setIfNull(this::getEnvironment, providedConfig::getEnvironment, this::setEnvironment);
            this.setIfNull(this::getRetryKnownIssues, providedConfig::getRetryKnownIssues, this::setRetryKnownIssues);
            this.setIfNull(this::getTreatSkipsAsFailures, providedConfig::getTreatSkipsAsFailures, this::setTreatSkipsAsFailures);
        }

    }

    @Data
    public static class Milestone implements Configuration<Milestone> {

        @EnvironmentVariable("REPORTING_MILESTONE_ID")
        @SystemProperty("reporting.milestone.id")
        @YamlProperty("reporting.milestone.id")
        @PropertiesFileProperty("reporting.milestone.id")
        private Long id;

        @EnvironmentVariable("REPORTING_MILESTONE_NAME")
        @SystemProperty("reporting.milestone.name")
        @YamlProperty("reporting.milestone.name")
        @PropertiesFileProperty("reporting.milestone.name")
        private String name;

        @Override
        public void copyMissing(Milestone providedConfig) {
            this.setIfNull(this::getId, providedConfig::getId, this::setId);
            this.setIfNull(this::getName, providedConfig::getName, this::setName);
        }

    }

    @Data
    public static class Notification implements Configuration<Notification> {

        @EnvironmentVariable("REPORTING_NOTIFICATION_ENABLED")
        @SystemProperty("reporting.notification.enabled")
        @YamlProperty("reporting.notification.enabled")
        @PropertiesFileProperty("reporting.notification.enabled")
        private Boolean enabled;

        @EnvironmentVariable("REPORTING_NOTIFICATION_NOTIFY_ON_EACH_FAILURE")
        @SystemProperty("reporting.notification.notify-on-each-failure")
        @YamlProperty("reporting.notification.notify-on-each-failure")
        @PropertiesFileProperty("reporting.notification.notify-on-each-failure")
        private Boolean notifyOnEachFailure;

        @EnvironmentVariable("REPORTING_NOTIFICATION_SLACK_CHANNELS")
        @SystemProperty("reporting.notification.slack-channels")
        @YamlProperty("reporting.notification.slack-channels")
        @PropertiesFileProperty("reporting.notification.slack-channels")
        private String slackChannels;

        @EnvironmentVariable("REPORTING_NOTIFICATION_MS_TEAMS_CHANNELS")
        @SystemProperty("reporting.notification.ms-teams-channels")
        @YamlProperty("reporting.notification.ms-teams-channels")
        @PropertiesFileProperty("reporting.notification.ms-teams-channels")
        private String msTeamsChannels;

        @EnvironmentVariable("REPORTING_NOTIFICATION_EMAILS")
        @SystemProperty("reporting.notification.emails")
        @YamlProperty("reporting.notification.emails")
        @PropertiesFileProperty("reporting.notification.emails")
        private String emails;

        @Override
        public void copyMissing(Notification providedConfig) {
            this.setIfNull(this::getEnabled, providedConfig::getEnabled, this::setEnabled);
            this.setIfNull(this::getNotifyOnEachFailure, providedConfig::getNotifyOnEachFailure, this::setNotifyOnEachFailure);
            this.setIfNull(this::getSlackChannels, providedConfig::getSlackChannels, this::setSlackChannels);
            this.setIfNull(this::getMsTeamsChannels, providedConfig::getMsTeamsChannels, this::setMsTeamsChannels);
            this.setIfNull(this::getEmails, providedConfig::getEmails, this::setEmails);
        }

    }

    @Data
    public static class Tcm implements Configuration<Tcm> {

        @InjectConfiguration
        private TestCaseStatus testCaseStatus = new TestCaseStatus();
        @InjectConfiguration
        private Zebrunner zebrunner = new Zebrunner();
        @InjectConfiguration
        private TestRail testRail = new TestRail();
        @InjectConfiguration
        private Xray xray = new Xray();
        @InjectConfiguration
        private Zephyr zephyr = new Zephyr();

        @Override
        public void copyMissing(Tcm providedConfig) {
            testCaseStatus.copyMissing(providedConfig.getTestCaseStatus());
            zebrunner.copyMissing(providedConfig.getZebrunner());
            testRail.copyMissing(providedConfig.getTestRail());
            xray.copyMissing(providedConfig.getXray());
            zephyr.copyMissing(providedConfig.getZephyr());
        }

        @Data
        public static class TestCaseStatus implements Configuration<TestCaseStatus> {

            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_PASS")
            @SystemProperty("reporting.tcm.test-case-status.on-pass")
            @YamlProperty("reporting.tcm.test-case-status.on-pass")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-pass")
            private String onPass;

            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_FAIL")
            @SystemProperty("reporting.tcm.test-case-status.on-fail")
            @YamlProperty("reporting.tcm.test-case-status.on-fail")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-fail")
            private String onFail;

            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_KNOWN_ISSUE")
            @SystemProperty("reporting.tcm.test-case-status.on-known-issue")
            @YamlProperty("reporting.tcm.test-case-status.on-known-issue")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-known-issue")
            private String onKnownIssue;

            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_SKIP")
            @SystemProperty("reporting.tcm.test-case-status.on-skip")
            @YamlProperty("reporting.tcm.test-case-status.on-skip")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-skip")
            private String onSkip;

            @EnvironmentVariable("REPORTING_TCM_TEST_CASE_STATUS_ON_BLOCK")
            @SystemProperty("reporting.tcm.test-case-status.on-block")
            @YamlProperty("reporting.tcm.test-case-status.on-block")
            @PropertiesFileProperty("reporting.tcm.test-case-status.on-block")
            private String onBlock;

            public boolean hasAnySpecified() {
                return onPass != null
                       || onFail != null
                       || onKnownIssue != null
                       || onSkip != null
                       || onBlock != null;
            }

            @Override
            public void copyMissing(TestCaseStatus providedConfig) {
                this.setIfNull(this::getOnPass, providedConfig::getOnPass, this::setOnPass);
                this.setIfNull(this::getOnFail, providedConfig::getOnFail, this::setOnFail);
                this.setIfNull(this::getOnKnownIssue, providedConfig::getOnKnownIssue, this::setOnKnownIssue);
                this.setIfNull(this::getOnSkip, providedConfig::getOnSkip, this::setOnSkip);
                this.setIfNull(this::getOnBlock, providedConfig::getOnBlock, this::setOnBlock);
            }

        }

        @Data
        public static class Zebrunner implements Configuration<Zebrunner> {

            @EnvironmentVariable("REPORTING_TCM_ZEBRUNNER_PUSH_RESULTS")
            @SystemProperty("reporting.tcm.zebrunner.push-results")
            @YamlProperty("reporting.tcm.zebrunner.push-results")
            @PropertiesFileProperty("reporting.tcm.zebrunner.push-results")
            private Boolean pushResults;

            @EnvironmentVariable("REPORTING_TCM_ZEBRUNNER_PUSH_IN_REAL_TIME")
            @SystemProperty("reporting.tcm.zebrunner.push-in-real-time")
            @YamlProperty("reporting.tcm.zebrunner.push-in-real-time")
            @PropertiesFileProperty("reporting.tcm.zebrunner.push-in-real-time")
            private Boolean pushInRealTime;

            @EnvironmentVariable("REPORTING_TCM_ZEBRUNNER_RUN_ID")
            @SystemProperty({"reporting.tcm.zebrunner.test-run-id", "reporting.tcm.zebrunner.run-id"})
            @YamlProperty({"reporting.tcm.zebrunner.test-run-id", "reporting.tcm.zebrunner.run-id"})
            @PropertiesFileProperty({"reporting.tcm.zebrunner.test-run-id", "reporting.tcm.zebrunner.run-id"})
            private String testRunId;

            @Override
            public void copyMissing(Zebrunner providedConfig) {
                this.setIfNull(this::getPushResults, providedConfig::getPushResults, this::setPushResults);
                this.setIfNull(this::getPushInRealTime, providedConfig::getPushInRealTime, this::setPushInRealTime);
                this.setIfNull(this::getTestRunId, providedConfig::getTestRunId, this::setTestRunId);
            }

        }

        @Data
        public static class TestRail implements Configuration<TestRail> {

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_PUSH_RESULTS")
            @SystemProperty("reporting.tcm.test-rail.push-results")
            @YamlProperty("reporting.tcm.test-rail.push-results")
            @PropertiesFileProperty("reporting.tcm.test-rail.push-results")
            private Boolean pushResults;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_PUSH_IN_REAL_TIME")
            @SystemProperty("reporting.tcm.test-rail.push-in-real-time")
            @YamlProperty("reporting.tcm.test-rail.push-in-real-time")
            @PropertiesFileProperty("reporting.tcm.test-rail.push-in-real-time")
            private Boolean pushInRealTime;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_SUITE_ID")
            @SystemProperty("reporting.tcm.test-rail.suite-id")
            @YamlProperty("reporting.tcm.test-rail.suite-id")
            @PropertiesFileProperty("reporting.tcm.test-rail.suite-id")
            private String suiteId;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_RUN_ID")
            @SystemProperty("reporting.tcm.test-rail.run-id")
            @YamlProperty("reporting.tcm.test-rail.run-id")
            @PropertiesFileProperty("reporting.tcm.test-rail.run-id")
            private String runId;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_INCLUDE_ALL_TEST_CASES_IN_NEW_RUN")
            @SystemProperty("reporting.tcm.test-rail.include-all-test-cases-in-new-run")
            @YamlProperty("reporting.tcm.test-rail.include-all-test-cases-in-new-run")
            @PropertiesFileProperty("reporting.tcm.test-rail.include-all-test-cases-in-new-run")
            private Boolean includeAllTestCasesInNewRun;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_RUN_NAME")
            @SystemProperty("reporting.tcm.test-rail.run-name")
            @YamlProperty("reporting.tcm.test-rail.run-name")
            @PropertiesFileProperty("reporting.tcm.test-rail.run-name")
            private String runName;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_MILESTONE_NAME")
            @SystemProperty("reporting.tcm.test-rail.milestone-name")
            @YamlProperty("reporting.tcm.test-rail.milestone-name")
            @PropertiesFileProperty("reporting.tcm.test-rail.milestone-name")
            private String milestoneName;

            @EnvironmentVariable("REPORTING_TCM_TEST_RAIL_ASSIGNEE")
            @SystemProperty("reporting.tcm.test-rail.assignee")
            @YamlProperty("reporting.tcm.test-rail.assignee")
            @PropertiesFileProperty("reporting.tcm.test-rail.assignee")
            private String assignee;

            @Override
            public void copyMissing(TestRail providedConfig) {
                this.setIfNull(this::getPushResults, providedConfig::getPushResults, this::setPushResults);
                this.setIfNull(this::getPushInRealTime, providedConfig::getPushInRealTime, this::setPushInRealTime);
                this.setIfNull(this::getSuiteId, providedConfig::getSuiteId, this::setSuiteId);
                this.setIfNull(this::getRunId, providedConfig::getRunId, this::setRunId);
                this.setIfNull(this::getIncludeAllTestCasesInNewRun, providedConfig::getIncludeAllTestCasesInNewRun, this::setIncludeAllTestCasesInNewRun);
                this.setIfNull(this::getRunName, providedConfig::getRunName, this::setRunName);
                this.setIfNull(this::getMilestoneName, providedConfig::getMilestoneName, this::setMilestoneName);
                this.setIfNull(this::getAssignee, providedConfig::getAssignee, this::setAssignee);
            }

        }

        @Data
        public static class Xray implements Configuration<Xray> {

            @EnvironmentVariable("REPORTING_TCM_XRAY_PUSH_RESULTS")
            @SystemProperty("reporting.tcm.xray.push-results")
            @YamlProperty("reporting.tcm.xray.push-results")
            @PropertiesFileProperty("reporting.tcm.xray.push-results")
            private Boolean pushResults;

            @EnvironmentVariable("REPORTING_TCM_XRAY_PUSH_IN_REAL_TIME")
            @SystemProperty("reporting.tcm.xray.push-in-real-time")
            @YamlProperty("reporting.tcm.xray.push-in-real-time")
            @PropertiesFileProperty("reporting.tcm.xray.push-in-real-time")
            private Boolean pushInRealTime;

            @EnvironmentVariable("REPORTING_TCM_XRAY_EXECUTION_KEY")
            @SystemProperty("reporting.tcm.xray.execution-key")
            @YamlProperty("reporting.tcm.xray.execution-key")
            @PropertiesFileProperty("reporting.tcm.xray.execution-key")
            private String executionKey;

            @Override
            public void copyMissing(Xray providedConfig) {
                this.setIfNull(this::getPushResults, providedConfig::getPushResults, this::setPushResults);
                this.setIfNull(this::getPushInRealTime, providedConfig::getPushInRealTime, this::setPushInRealTime);
                this.setIfNull(this::getExecutionKey, providedConfig::getExecutionKey, this::setExecutionKey);
            }

        }

        @Data
        public static class Zephyr implements Configuration<Zephyr> {

            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_PUSH_RESULTS")
            @SystemProperty("reporting.tcm.zephyr.push-results")
            @YamlProperty("reporting.tcm.zephyr.push-results")
            @PropertiesFileProperty("reporting.tcm.zephyr.push-results")
            private Boolean pushResults;

            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_PUSH_IN_REAL_TIME")
            @SystemProperty("reporting.tcm.zephyr.push-in-real-time")
            @YamlProperty("reporting.tcm.zephyr.push-in-real-time")
            @PropertiesFileProperty("reporting.tcm.zephyr.push-in-real-time")
            private Boolean pushInRealTime;

            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_JIRA_PROJECT_KEY")
            @SystemProperty("reporting.tcm.zephyr.jira-project-key")
            @YamlProperty("reporting.tcm.zephyr.jira-project-key")
            @PropertiesFileProperty("reporting.tcm.zephyr.jira-project-key")
            private String jiraProjectKey;

            @EnvironmentVariable("REPORTING_TCM_ZEPHYR_TEST_CYCLE_KEY")
            @SystemProperty("reporting.tcm.zephyr.test-cycle-key")
            @YamlProperty("reporting.tcm.zephyr.test-cycle-key")
            @PropertiesFileProperty("reporting.tcm.zephyr.test-cycle-key")
            private String testCycleKey;

            @Override
            public void copyMissing(Zephyr providedConfig) {
                this.setIfNull(this::getPushResults, providedConfig::getPushResults, this::setPushResults);
                this.setIfNull(this::getPushInRealTime, providedConfig::getPushInRealTime, this::setPushInRealTime);
                this.setIfNull(this::getJiraProjectKey, providedConfig::getJiraProjectKey, this::setJiraProjectKey);
                this.setIfNull(this::getTestCycleKey, providedConfig::getTestCycleKey, this::setTestCycleKey);
            }

        }

    }

}
