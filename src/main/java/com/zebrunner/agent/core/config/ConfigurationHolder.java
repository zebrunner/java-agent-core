package com.zebrunner.agent.core.config;

import java.util.Set;

public class ConfigurationHolder {

    private static final boolean REPORTING_ENABLED;
    private static final String PROJECT_KEY;
    private static final String HOST;
    private static final String TOKEN;
    private static final String RUN_DISPLAY_NAME;
    private static final String RUN_BUILD;
    private static final String RUN_ENVIRONMENT;
    private static final String RERUN_RUN_ID;
    private static final Set<String> SLACK_CHANNELS;
    private static final Set<String> MICROSOFT_TEAMS_CHANNELS;
    private static final Set<String> EMAILS;

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

        RERUN_RUN_ID = configuration.getRerun().getRunId();

        SLACK_CHANNELS = configuration.getNotification().getSlackChannels();
        MICROSOFT_TEAMS_CHANNELS = configuration.getNotification().getMicrosoftTeamsChannels();
        EMAILS = configuration.getNotification().getEmails();
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

    public static String getRerunRunId() {
        return RERUN_RUN_ID;
    }

    public static Set<String> getSlackChannels() {
        return SLACK_CHANNELS;
    }

    public static Set<String> getMicrosoftTeamsChannels() {
        return MICROSOFT_TEAMS_CHANNELS;
    }

    public static Set<String> getEmails() {
        return EMAILS;
    }
}
