package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.config.provider.EnvironmentConfigurationProvider;
import com.zebrunner.agent.core.config.provider.PropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.SystemPropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.YamlConfigurationProvider;
import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
class ConfigurationProvidersChain {

    private static final ConfigurationProvidersChain INSTANCE = new ConfigurationProvidersChain();
    private static final String DEFAULT_PROJECT = "DEF";

    public static ConfigurationProvidersChain getInstance() {
        return INSTANCE;
    }

    @Getter
    private final List<ConfigurationProvider> configurationProviders;

    private ConfigurationProvidersChain() {
        configurationProviders = new ArrayList<>(Arrays.asList(
                new EnvironmentConfigurationProvider(),
                new SystemPropertiesConfigurationProvider(),
                new YamlConfigurationProvider(),
                new PropertiesConfigurationProvider()
        ));
    }

    public ReportingConfiguration getConfiguration() {
        ReportingConfiguration config = ReportingConfiguration.builder()
                                                              .run(new ReportingConfiguration.RunConfiguration())
                                                              .server(new ReportingConfiguration.ServerConfiguration())
                                                              .milestone(new ReportingConfiguration.MilestoneConfiguration())
                                                              .notification(new ReportingConfiguration.NotificationConfiguration())
                                                              .tcm(new ReportingConfiguration.TcmConfiguration())
                                                              .build();
        assembleConfiguration(config);
        if (areMandatoryArgsSet(config)) {
            return config;
        } else {
            throw new TestAgentException("Mandatory agent properties are missing - double-check agent configuration");
        }
    }

    /**
     * Iterates over all configuration providers and assembles agent configuration. Configuration property
     * supplied by provider with highest priority always takes precedence.
     *
     * @param config configuration to be assembled
     */
    private void assembleConfiguration(ReportingConfiguration config) {
        for (ConfigurationProvider provider : configurationProviders) {
            try {
                ReportingConfiguration providedConfig = provider.getConfiguration();
                normalize(providedConfig);
                merge(config, providedConfig);
                // no need to iterate further to provider with lower priority if all args are provided already
                if (areAllArgsSet(providedConfig)) {
                    break;
                }
            } catch (TestAgentException e) {
                log.warn(e.getMessage());
            }
        }
        if (config.getProjectKey() == null || config.getProjectKey().trim().isEmpty()) {
            config.setProjectKey(DEFAULT_PROJECT);
        }
    }

    private static void normalize(ReportingConfiguration config) {
        normalizeServerConfiguration(config);
        normalizeRunConfiguration(config);
        normalizeMilestoneConfiguration(config);
        normalizeNotificationConfiguration(config);
        normalizeTcmConfiguration(config);
    }

    private static void normalizeServerConfiguration(ReportingConfiguration config) {
        if (config.getServer() == null) {
            config.setServer(new ReportingConfiguration.ServerConfiguration());
        } else {
            ReportingConfiguration.ServerConfiguration serverConfig = config.getServer();

            String hostname = serverConfig.getHostname();
            String accessToken = serverConfig.getHostname();

            if (hostname != null && accessToken.trim().isEmpty()) {
                serverConfig.setHostname(null);
            }
            if (accessToken != null && accessToken.trim().isEmpty()) {
                serverConfig.setAccessToken(null);
            }
        }
    }

    private static void normalizeRunConfiguration(ReportingConfiguration config) {
        if (config.getRun() == null) {
            config.setRun(new ReportingConfiguration.RunConfiguration());
        } else {
            ReportingConfiguration.RunConfiguration runConfig = config.getRun();

            String displayName = runConfig.getDisplayName();
            String build = runConfig.getBuild();
            String environment = runConfig.getEnvironment();
            String context = runConfig.getContext();

            if (displayName != null && displayName.trim().isEmpty()) {
                runConfig.setDisplayName(null);
            }
            if (build != null && build.trim().isEmpty()) {
                runConfig.setBuild(null);
            }
            if (environment != null && environment.trim().isEmpty()) {
                runConfig.setEnvironment(null);
            }
            if (context != null && context.trim().isEmpty()) {
                runConfig.setContext(null);
            }
        }
    }

    private static void normalizeMilestoneConfiguration(ReportingConfiguration config) {
        if (config.getMilestone() == null) {
            config.setMilestone(new ReportingConfiguration.MilestoneConfiguration(null, null));
        } else {
            ReportingConfiguration.MilestoneConfiguration milestoneConfig = config.getMilestone();

            String name = milestoneConfig.getName();
            if (name != null && name.trim().isEmpty()) {
                milestoneConfig.setName(null);
            }
        }
    }

    private static void normalizeNotificationConfiguration(ReportingConfiguration config) {
        if (config.getNotification() == null) {
            config.setNotification(new ReportingConfiguration.NotificationConfiguration(null, null, null, null, null));
        } else {
            ReportingConfiguration.NotificationConfiguration notificationConfig = config.getNotification();

            String slackChannels = notificationConfig.getSlackChannels();
            if (slackChannels != null && slackChannels.isEmpty()) {
                notificationConfig.setSlackChannels(null);
            }

            String msTeamsChannels = notificationConfig.getMsTeamsChannels();
            if (msTeamsChannels != null && msTeamsChannels.isEmpty()) {
                notificationConfig.setMsTeamsChannels(null);
            }

            String emails = notificationConfig.getEmails();
            if (emails != null && emails.isEmpty()) {
                notificationConfig.setEmails(null);
            }
        }
    }

    private static void normalizeTcmConfiguration(ReportingConfiguration config) {
        ReportingConfiguration.TcmConfiguration tcmConfig = config.getTcm();

        if (tcmConfig == null) {
            config.setTcm(new ReportingConfiguration.TcmConfiguration());
        } else {
            normalizeTcmTestCaseStatus(tcmConfig);
            normalizeZebrunnerTcmConfiguration(tcmConfig);
            normalizeTestRailConfiguration(tcmConfig);
            normalizeXrayConfiguration(tcmConfig);
            normalizeZephyrConfiguration(tcmConfig);
        }
    }

    private static void normalizeTcmTestCaseStatus(ReportingConfiguration.TcmConfiguration tcmConfiguration) {
        ReportingConfiguration.TcmConfiguration.TestCaseStatus testCaseStatus = tcmConfiguration.getTestCaseStatus();
        if (testCaseStatus == null) {
            tcmConfiguration.setTestCaseStatus(new ReportingConfiguration.TcmConfiguration.TestCaseStatus());
        } else {
            String onPass = testCaseStatus.getOnPass();
            String onFail = testCaseStatus.getOnFail();
            String onSkip = testCaseStatus.getOnSkip();

            if (onPass != null && onPass.trim().isEmpty()) {
                testCaseStatus.setOnPass(null);
            }
            if (onFail != null && onFail.trim().isEmpty()) {
                testCaseStatus.setOnFail(null);
            }
            if (onSkip != null && onSkip.trim().isEmpty()) {
                testCaseStatus.setOnSkip(null);
            }
        }
    }

    private static void normalizeZebrunnerTcmConfiguration(ReportingConfiguration.TcmConfiguration tcmConfig) {
        ReportingConfiguration.TcmConfiguration.Zebrunner zebrunnerConfig = tcmConfig.getZebrunner();

        if (zebrunnerConfig == null) {
            tcmConfig.setZebrunner(new ReportingConfiguration.TcmConfiguration.Zebrunner());
        } else {
            String testRunId = zebrunnerConfig.getTestRunId();

            if (testRunId != null && testRunId.trim().isEmpty()) {
                zebrunnerConfig.setTestRunId(null);
            }
        }
    }

    private static void normalizeTestRailConfiguration(ReportingConfiguration.TcmConfiguration tcmConfig) {
        ReportingConfiguration.TcmConfiguration.TestRail testRailConfig = tcmConfig.getTestRail();

        if (testRailConfig == null) {
            tcmConfig.setTestRail(new ReportingConfiguration.TcmConfiguration.TestRail());
        } else {
            String testRailSuiteId = testRailConfig.getSuiteId();
            String testRailRunId = testRailConfig.getRunId();
            String testRailRunName = testRailConfig.getRunName();
            String testRailMilestoneName = testRailConfig.getMilestoneName();
            String assignee = testRailConfig.getAssignee();

            if (testRailSuiteId != null && testRailSuiteId.trim().isEmpty()) {
                testRailConfig.setSuiteId(null);
            }
            if (testRailRunId != null && testRailRunId.trim().isEmpty()) {
                testRailConfig.setRunId(null);
            }
            if (testRailRunName != null && testRailRunName.trim().isEmpty()) {
                testRailConfig.setRunName(null);
            }
            if (testRailMilestoneName != null && testRailMilestoneName.trim().isEmpty()) {
                testRailConfig.setMilestoneName(null);
            }
            if (assignee != null && assignee.trim().isEmpty()) {
                testRailConfig.setAssignee(null);
            }
        }
    }

    private static void normalizeXrayConfiguration(ReportingConfiguration.TcmConfiguration tcmConfig) {
        ReportingConfiguration.TcmConfiguration.Xray xrayConfig = tcmConfig.getXray();

        if (xrayConfig == null) {
            tcmConfig.setXray(new ReportingConfiguration.TcmConfiguration.Xray());
        } else {
            String executionKey = xrayConfig.getExecutionKey();

            if (executionKey != null && executionKey.trim().isEmpty()) {
                xrayConfig.setExecutionKey(null);
            }
        }
    }

    private static void normalizeZephyrConfiguration(ReportingConfiguration.TcmConfiguration tcmConfig) {
        ReportingConfiguration.TcmConfiguration.Zephyr zephyrConfig = tcmConfig.getZephyr();

        if (zephyrConfig == null) {
            tcmConfig.setZephyr(new ReportingConfiguration.TcmConfiguration.Zephyr());
        } else {
            String testCycleKey = zephyrConfig.getTestCycleKey();
            String jiraProjectKey = zephyrConfig.getJiraProjectKey();

            if (testCycleKey != null && testCycleKey.trim().isEmpty()) {
                zephyrConfig.setTestCycleKey(null);
            }
            if (jiraProjectKey != null && jiraProjectKey.trim().isEmpty()) {
                zephyrConfig.setJiraProjectKey(null);
            }
        }
    }

    /**
     * Sets values coming from provided configuration that were not set previously by providers with higher priority
     *
     * @param config         configuration assembled by previous configuration providers
     * @param providedConfig configuration from current configuration provider
     */
    private static void merge(ReportingConfiguration config, ReportingConfiguration providedConfig) {
        if (config.getReportingEnabled() == null) {
            config.setReportingEnabled(providedConfig.getReportingEnabled());
        }

        if (config.getProjectKey() == null) {
            config.setProjectKey(providedConfig.getProjectKey());
        }

        ReportingConfiguration.ServerConfiguration server = config.getServer();
        if (server.getHostname() == null) {
            server.setHostname(providedConfig.getServer().getHostname());
        }
        if (server.getAccessToken() == null) {
            server.setAccessToken(providedConfig.getServer().getAccessToken());
        }

        ReportingConfiguration.RunConfiguration run = config.getRun();
        if (run.getDisplayName() == null) {
            run.setDisplayName(providedConfig.getRun().getDisplayName());
        }
        if (run.getBuild() == null) {
            run.setBuild(providedConfig.getRun().getBuild());
        }
        if (run.getEnvironment() == null) {
            run.setEnvironment(providedConfig.getRun().getEnvironment());
        }
        if (run.getContext() == null) {
            run.setContext(providedConfig.getRun().getContext());
        }
        if (run.getRetryKnownIssues() == null) {
            run.setRetryKnownIssues(providedConfig.getRun().getRetryKnownIssues());
        }
        if (run.getSubstituteRemoteWebDrivers() == null) {
            run.setSubstituteRemoteWebDrivers(providedConfig.getRun().getSubstituteRemoteWebDrivers());
        }
        if (run.getTreatSkipsAsFailures() == null) {
            run.setTreatSkipsAsFailures(providedConfig.getRun().getTreatSkipsAsFailures());
        }

        ReportingConfiguration.NotificationConfiguration notification = config.getNotification();
        if (notification.getEnabled() == null) {
            notification.setEnabled(providedConfig.getNotification().getEnabled());
        }
        if (notification.getNotifyOnEachFailure() == null) {
            notification.setNotifyOnEachFailure(providedConfig.getNotification().getNotifyOnEachFailure());
        }
        if (notification.getSlackChannels() == null) {
            notification.setSlackChannels(providedConfig.getNotification().getSlackChannels());
        }
        if (notification.getMsTeamsChannels() == null) {
            notification.setMsTeamsChannels(providedConfig.getNotification().getMsTeamsChannels());
        }
        if (notification.getEmails() == null) {
            notification.setEmails(providedConfig.getNotification().getEmails());
        }

        ReportingConfiguration.MilestoneConfiguration milestone = config.getMilestone();
        if (milestone.getId() == null) {
            milestone.setId(providedConfig.getMilestone().getId());
        }
        if (milestone.getName() == null) {
            milestone.setName(providedConfig.getMilestone().getName());
        }

        ReportingConfiguration.TcmConfiguration tcm = config.getTcm();
        if (tcm == null) {
            tcm = providedConfig.getTcm();
            config.setTcm(tcm);
        }

        ReportingConfiguration.TcmConfiguration.TestCaseStatus testCaseStatus = tcm.getTestCaseStatus();
        if (testCaseStatus.getOnPass() == null) {
            testCaseStatus.setOnPass(providedConfig.getTcm().getTestCaseStatus().getOnPass());
        }
        if (testCaseStatus.getOnFail() == null) {
            testCaseStatus.setOnFail(providedConfig.getTcm().getTestCaseStatus().getOnFail());
        }
        if (testCaseStatus.getOnSkip() == null) {
            testCaseStatus.setOnSkip(providedConfig.getTcm().getTestCaseStatus().getOnSkip());
        }

        ReportingConfiguration.TcmConfiguration.Zebrunner zebrunner = tcm.getZebrunner();
        if (zebrunner.getPushResults() == null) {
            zebrunner.setPushResults(providedConfig.getTcm().getZebrunner().getPushResults());
        }
        if (zebrunner.getPushInRealTime() == null) {
            zebrunner.setPushInRealTime(providedConfig.getTcm().getZebrunner().getPushInRealTime());
        }
        if (zebrunner.getTestRunId() == null) {
            zebrunner.setTestRunId(providedConfig.getTcm().getZebrunner().getTestRunId());
        }

        ReportingConfiguration.TcmConfiguration.TestRail testRail = tcm.getTestRail();
        if (testRail.getPushResults() == null) {
            testRail.setPushResults(providedConfig.getTcm().getTestRail().getPushResults());
        }
        if (testRail.getPushInRealTime() == null) {
            testRail.setPushInRealTime(providedConfig.getTcm().getTestRail().getPushInRealTime());
        }
        if (testRail.getIncludeAllTestCasesInNewRun() == null) {
            testRail.setIncludeAllTestCasesInNewRun(providedConfig.getTcm().getTestRail().getIncludeAllTestCasesInNewRun());
        }
        if (testRail.getSuiteId() == null) {
            testRail.setSuiteId(providedConfig.getTcm().getTestRail().getSuiteId());
        }
        if (testRail.getRunId() == null) {
            testRail.setRunId(providedConfig.getTcm().getTestRail().getRunId());
        }
        if (testRail.getRunName() == null) {
            testRail.setRunName(providedConfig.getTcm().getTestRail().getRunName());
        }
        if (testRail.getMilestoneName() == null) {
            testRail.setMilestoneName(providedConfig.getTcm().getTestRail().getMilestoneName());
        }
        if (testRail.getAssignee() == null) {
            testRail.setAssignee(providedConfig.getTcm().getTestRail().getAssignee());
        }

        ReportingConfiguration.TcmConfiguration.Xray xray = tcm.getXray();
        if (xray.getPushResults() == null) {
            xray.setPushResults(providedConfig.getTcm().getXray().getPushResults());
        }
        if (xray.getPushInRealTime() == null) {
            xray.setPushInRealTime(providedConfig.getTcm().getXray().getPushInRealTime());
        }
        if (xray.getExecutionKey() == null) {
            xray.setExecutionKey(providedConfig.getTcm().getXray().getExecutionKey());
        }

        ReportingConfiguration.TcmConfiguration.Zephyr zephyr = tcm.getZephyr();
        if (zephyr.getPushResults() == null) {
            zephyr.setPushResults(providedConfig.getTcm().getZephyr().getPushResults());
        }
        if (zephyr.getPushInRealTime() == null) {
            zephyr.setPushInRealTime(providedConfig.getTcm().getZephyr().getPushInRealTime());
        }
        if (zephyr.getJiraProjectKey() == null) {
            zephyr.setJiraProjectKey(providedConfig.getTcm().getZephyr().getJiraProjectKey());
        }
        if (zephyr.getTestCycleKey() == null) {
            zephyr.setTestCycleKey(providedConfig.getTcm().getZephyr().getTestCycleKey());
        }
    }

    // project-key is not considered as a mandatory property
    private static boolean areMandatoryArgsSet(ReportingConfiguration config) {
        ReportingConfiguration.ServerConfiguration server = config.getServer();

        // no need to check anything if reporting is disabled
        return !config.isReportingEnabled() || (server.getHostname() != null && server.getAccessToken() != null);
    }

    private static boolean areAllArgsSet(ReportingConfiguration config) {
        Boolean enabled = config.getReportingEnabled();

        String projectKey = config.getProjectKey();
        String hostname = config.getServer().getHostname();
        String accessToken = config.getServer().getAccessToken();

        String displayName = config.getRun().getDisplayName();
        String build = config.getRun().getBuild();
        String environment = config.getRun().getEnvironment();
        String context = config.getRun().getContext();
        Boolean retryKnownIssues = config.getRun().getRetryKnownIssues();
        Boolean substituteRemoteWebDrivers = config.getRun().getSubstituteRemoteWebDrivers();
        Boolean treatSkipsAsFailures = config.getRun().getTreatSkipsAsFailures();

        String testCaseStatusOnPass = config.getTcm().getTestCaseStatus().getOnPass();
        String testCaseStatusOnFail = config.getTcm().getTestCaseStatus().getOnFail();
        String testCaseStatusOnSkip = config.getTcm().getTestCaseStatus().getOnSkip();

        Boolean notificationsEnabled = config.getNotification().getEnabled();
        Boolean notifyOnEachFailure = config.getNotification().getNotifyOnEachFailure();
        String slackChannels = config.getNotification().getSlackChannels();
        String msTeamsChannels = config.getNotification().getMsTeamsChannels();
        String emails = config.getNotification().getEmails();

        Boolean tcmPushResults = config.getTcm().getZebrunner().getPushResults();
        Boolean tcmPushInRealTime = config.getTcm().getZebrunner().getPushInRealTime();
        String tcmRunId = config.getTcm().getZebrunner().getTestRunId();

        Boolean testRailPushResults = config.getTcm().getTestRail().getPushResults();
        Boolean testRailPushInRealTime = config.getTcm().getTestRail().getPushInRealTime();
        String testRailSuiteId = config.getTcm().getTestRail().getSuiteId();
        String testRailRunId = config.getTcm().getTestRail().getRunId();
        Boolean testRailIncludeAllTestCasesInNewRun = config.getTcm().getTestRail().getIncludeAllTestCasesInNewRun();
        String testRailRunName = config.getTcm().getTestRail().getRunName();
        String testRailMilestoneName = config.getTcm().getTestRail().getMilestoneName();
        String testRailAssignee = config.getTcm().getTestRail().getAssignee();

        Boolean xrayPushResults = config.getTcm().getXray().getPushResults();
        Boolean xrayPushInRealTime = config.getTcm().getXray().getPushInRealTime();
        String xrayExecutionKey = config.getTcm().getXray().getExecutionKey();

        Boolean zephyrPushResults = config.getTcm().getZephyr().getPushResults();
        Boolean zephyrPushInRealTime = config.getTcm().getZephyr().getPushInRealTime();
        String zephyrJiraProjectKey = config.getTcm().getZephyr().getJiraProjectKey();
        String zephyrTestCycleKey = config.getTcm().getZephyr().getTestCycleKey();

        return enabled != null
                && projectKey != null
                && hostname != null && accessToken != null
                && displayName != null && build != null && environment != null && context != null
                && retryKnownIssues != null && substituteRemoteWebDrivers != null && treatSkipsAsFailures != null
                && testCaseStatusOnPass != null && testCaseStatusOnFail != null && testCaseStatusOnSkip != null
                && notificationsEnabled != null && notifyOnEachFailure != null && slackChannels != null && msTeamsChannels != null && emails != null
                && tcmPushResults != null && tcmPushInRealTime != null && tcmRunId != null
                && testRailPushResults != null && testRailPushInRealTime != null && testRailSuiteId != null
                && testRailRunId != null && testRailIncludeAllTestCasesInNewRun != null && testRailRunName != null
                && testRailMilestoneName != null && testRailAssignee != null
                && xrayPushResults != null && xrayPushInRealTime != null && xrayExecutionKey != null
                && zephyrPushResults != null && zephyrPushInRealTime != null && zephyrJiraProjectKey != null && zephyrTestCycleKey != null;
    }

}
