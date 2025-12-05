package com.zebrunner.agent.core.config;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.zebrunner.agent.core.registrar.domain.NotificationTarget;

public class ConfigurationHolder {

    private static final ConfigurationProvidersChain CONFIGURATION_PROVIDERS_CHAIN = ConfigurationProvidersChain.getInstance();
    private static ReportingConfiguration configuration = CONFIGURATION_PROVIDERS_CHAIN.getConfiguration();

    public static void addConfigurationProviderAfter(ConfigurationProvider configurationProvider,
                                                     Class<? extends ConfigurationProvider> afterProviderWithClass) {
        List<ConfigurationProvider> configurationProviders = CONFIGURATION_PROVIDERS_CHAIN.getConfigurationProviders();

        boolean added = false;
        // no reason to check the latest provider because anyway we will add after it
        for (int i = 0; i < configurationProviders.size() - 1; i++) {
            ConfigurationProvider existingConfigurationProvider = configurationProviders.get(i);
            if (afterProviderWithClass.isInstance(existingConfigurationProvider)) {
                configurationProviders.add(i + 1, configurationProvider);
                added = true;
            }
        }

        if (!added) {
            configurationProviders.add(configurationProvider);
        }

        configuration = CONFIGURATION_PROVIDERS_CHAIN.getConfiguration();
    }

    public static ReportingConfiguration get() {
        return configuration;
    }

    public static boolean isReportingEnabled() {
        return configuration.isReportingEnabled();
    }

    public static String getProjectKey() {
        return configuration.getProjectKey();
    }

    public static String getHost() {
        return configuration.getServer().getHostname();
    }

    public static String getToken() {
        return configuration.getServer().getAccessToken();
    }

    public static String getRunDisplayNameOr(String displayName) {
        String RUN_DISPLAY_NAME = configuration.getRun().getDisplayName();
        return RUN_DISPLAY_NAME != null ? RUN_DISPLAY_NAME : displayName;
    }

    public static String getRunBuild() {
        return configuration.getRun().getBuild();
    }

    public static String getRunEnvironment() {
        return configuration.getRun().getEnvironment();
    }

    public static String getRunContext() {
        return Optional.ofNullable(System.getProperty("ci_run_id"))
                       .map(ConfigurationHolder::toSerializedRunContext)
                       .orElseGet(() -> configuration.getRun().getContext());
    }

    public static boolean shouldRetryKnownIssues() {
        Boolean retryKnownIssues = configuration.getRun().getRetryKnownIssues();
        return retryKnownIssues != null && retryKnownIssues;
    }

    public static boolean shouldSubstituteRemoteWebDrivers() {
        Boolean substituteRemoteWebDrivers = configuration.getRun().getSubstituteRemoteWebDrivers();
        return substituteRemoteWebDrivers != null && substituteRemoteWebDrivers;
    }

    public static boolean shouldTreatSkipsAsFailures() {
        Boolean treatSkipsAsFailures = configuration.getRun().getTreatSkipsAsFailures();
        return treatSkipsAsFailures == null || treatSkipsAsFailures;
    }

    public static ReportingConfiguration.Tcm.TestCaseStatus getTestCaseStatus() {
        return configuration.getTcm().getTestCaseStatus();
    }

    public static String getTestCaseStatusOnPass() {
        return configuration.getTcm().getTestCaseStatus().getOnPass();
    }

    public static String getTestCaseStatusOnFail() {
        return configuration.getTcm().getTestCaseStatus().getOnFail();
    }

    public static String getTestCaseStatusOnSkip() {
        return configuration.getTcm().getTestCaseStatus().getOnSkip();
    }

    public static String getTestCaseStatusOnBlock() {
        return configuration.getTcm().getTestCaseStatus().getOnBlock();
    }

    private static String toSerializedRunContext(String ciRunId) {
        Map<String, Object> runContext = new HashMap<>();
        runContext.put("id", ciRunId);
        if ("true".equalsIgnoreCase(System.getProperty("rerun_failures"))) {
            runContext.put("rerunOnlyFailures", true);
            runContext.put("statuses", Arrays.asList("FAILED", "SKIPPED", "ABORTED", "IN_PROGRESS"));
        }
        return new Gson().toJson(runContext);
    }

//    private static String toSerializedRunContext(String ciRunId) {
//        Map<String, Object> runContext = new HashMap<>();
//        runContext.put("testRunUuid", ciRunId);
//        runContext.put("mode", "LEGACY");
//        if ("true".equalsIgnoreCase(System.getProperty("rerun_failures"))) {
//            Map<String, Object> rerunCriteria = new HashMap<>();
//            rerunCriteria.put("anyOfStatuses", Arrays.asList("FAILED", "SKIPPED", "ABORTED"));
//            rerunCriteria.put("knownIssue", false);
//
//            runContext.put("rerunCriteria", rerunCriteria);
//            runContext.put("mode", "RERUN");
//        }
//        return new Gson().toJson(runContext);
//    }

    public static boolean notificationsEnabled() {
        Boolean enabled = configuration.getNotification().getEnabled();
        return enabled == null || enabled;
    }

    public static boolean shouldNotifyOnEachFailure() {
        Boolean notifyOnEachFailure = configuration.getNotification().getNotifyOnEachFailure();
        return notifyOnEachFailure != null && notifyOnEachFailure;
    }

    public static List<NotificationTarget> collectNotificationTargets() {
        List<NotificationTarget> notificationTargets = new ArrayList<>();

        String emailRecipients = configuration.getNotification().getEmails();
        if (emailRecipients != null && !emailRecipients.isEmpty()) {
            notificationTargets.add(NotificationTarget.email(emailRecipients));
        }

        String teamsChannels = configuration.getNotification().getMsTeamsChannels();
        if (teamsChannels != null && !teamsChannels.isEmpty()) {
            notificationTargets.add(NotificationTarget.teams(teamsChannels));
        }

        String slackChannels = configuration.getNotification().getSlackChannels();
        if (slackChannels != null && !slackChannels.isEmpty()) {
            notificationTargets.add(NotificationTarget.slack(slackChannels));
        }

        return notificationTargets;
    }

    public static Long getMilestoneId() {
        return configuration.getMilestone().getId();
    }

    public static String getMilestoneName() {
        return configuration.getMilestone().getName();
    }

    public static boolean isTcmSyncEnabled() {
        Boolean pushResults = configuration.getTcm().getZebrunner().getPushResults();
        return pushResults == null || pushResults;
    }

    public static boolean isTcmRealTimeSyncEnabled() {
        Boolean pushInRealTime = configuration.getTcm().getZebrunner().getPushInRealTime();
        return pushInRealTime != null && pushInRealTime;
    }

    public static String getTcmTestRunId() {
        return configuration.getTcm().getZebrunner().getTestRunId();
    }

    public static boolean isTestRailSyncEnabled() {
        Boolean pushResults = configuration.getTcm().getTestRail().getPushResults();
        return pushResults == null || pushResults;
    }

    public static boolean isTestRailRealTimeSyncEnabled() {
        Boolean pushInRealTime = configuration.getTcm().getTestRail().getPushInRealTime();
        return pushInRealTime != null && pushInRealTime;
    }

    public static boolean shouldTestRailIncludeAllTestCasesInNewRun() {
        Boolean includeAllTestCasesInNewRun = configuration.getTcm().getTestRail().getIncludeAllTestCasesInNewRun();
        return includeAllTestCasesInNewRun != null && includeAllTestCasesInNewRun;
    }

    public static String getTestRailSuiteId() {
        return configuration.getTcm().getTestRail().getSuiteId();
    }

    public static String getTestRailRunId() {
        return configuration.getTcm().getTestRail().getRunId();
    }

    public static String getTestRailRunName() {
        return configuration.getTcm().getTestRail().getRunName();
    }

    public static String getTestRailMilestoneName() {
        return configuration.getTcm().getTestRail().getMilestoneName();
    }

    public static String getTestRailAssignee() {
        return configuration.getTcm().getTestRail().getAssignee();
    }

    public static boolean isXraySyncEnabled() {
        Boolean pushResults = configuration.getTcm().getXray().getPushResults();
        return pushResults == null || pushResults;
    }

    public static boolean isXrayRealTimeSyncEnabled() {
        Boolean pushInRealTime = configuration.getTcm().getXray().getPushInRealTime();
        return pushInRealTime != null && pushInRealTime;
    }

    public static String getXrayExecutionKey() {
        return configuration.getTcm().getXray().getExecutionKey();
    }

    public static boolean isZephyrSyncEnabled() {
        Boolean pushResults = configuration.getTcm().getZephyr().getPushResults();
        return pushResults == null || pushResults;
    }

    public static boolean isZephyrSyncRealTimeEnabled() {
        Boolean pushInRealTime = configuration.getTcm().getZephyr().getPushInRealTime();
        return pushInRealTime != null && pushInRealTime;
    }

    public static String getZephyrJiraProjectKey() {
        return configuration.getTcm().getZephyr().getJiraProjectKey();
    }

    public static String getZephyrTestCycleKey() {
        return configuration.getTcm().getZephyr().getTestCycleKey();
    }

}
