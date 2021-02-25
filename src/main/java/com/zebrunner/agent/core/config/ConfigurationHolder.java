package com.zebrunner.agent.core.config;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigurationHolder {

    private static final boolean REPORTING_ENABLED;
    private static final String PROJECT_KEY;
    private static final String HOST;
    private static final String TOKEN;
    private static final String RUN_DISPLAY_NAME;
    private static final String RUN_BUILD;
    private static final String RUN_ENVIRONMENT;
    private static final String RERUN_CONDITION;
    private static final String SLACK_CHANNELS;
    private static final String MS_TEAMS_CHANNELS;
    private static final String EMAILS;

    static {
        ConfigurationProvider configurationProvider = DefaultConfigurationProviderChain.getInstance();
        ReportingConfiguration configuration = configurationProvider.getConfiguration();

        REPORTING_ENABLED = configuration.isReportingEnabled();
        PROJECT_KEY = configuration.getProjectKey();

        HOST = configuration.getServer().getHostname();
        TOKEN = configuration.getServer().getAccessToken();

        RUN_DISPLAY_NAME = configuration.getRun().getDisplayName();
        RUN_BUILD = configuration.getRun().getBuild();
        RUN_ENVIRONMENT = configuration.getRun().getEnvironment();

        RERUN_CONDITION = Optional.ofNullable(System.getProperty("ci_run_id"))
                                  .map(ConfigurationHolder::toSerializedRerunCondition)
                                  .orElseGet(configuration::getRerunCondition);

        SLACK_CHANNELS = configuration.getNotification().getSlackChannels();
        MS_TEAMS_CHANNELS = configuration.getNotification().getMsTeamsChannels();
        EMAILS = configuration.getNotification().getEmails();
    }

    private static String toSerializedRerunCondition(String ciRunId) {
        Map<String, Object> rerunCondition = new HashMap<>();
        rerunCondition.put("ciRunId", ciRunId);
        if ("true".equalsIgnoreCase(System.getProperty("rerun_failures"))) {
            rerunCondition.put("onlyFailures", true);
            rerunCondition.put("excludeKnownIssues", true);
            rerunCondition.put("statuses", Arrays.asList("FAILED", "SKIPPED", "ABORTED", "IN_PROGRESS"));
        }
        return new Gson().toJson(rerunCondition);
    }

    public static boolean isReportingEnabled() {
        return REPORTING_ENABLED;
    }

    public static String getProjectKey() {
        return PROJECT_KEY;
    }

    public static String getHost() {
        return HOST;
    }

    public static String getToken() {
        return TOKEN;
    }

    public static String getRunDisplayNameOr(String displayName) {
        return RUN_DISPLAY_NAME != null ? RUN_DISPLAY_NAME : displayName;
    }

    public static String getRunBuild() {
        return RUN_BUILD;
    }

    public static String getRunEnvironment() {
        return RUN_ENVIRONMENT;
    }

    public static String getRerunCondition() {
        return RERUN_CONDITION;
    }

    public static String getSlackChannels() {
        return SLACK_CHANNELS;
    }

    public static String getMsTeamsChannels() {
        return MS_TEAMS_CHANNELS;
    }

    public static String getEmails() {
        return EMAILS;
    }
}
