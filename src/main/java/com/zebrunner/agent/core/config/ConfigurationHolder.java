package com.zebrunner.agent.core.config;

public class ConfigurationHolder {

    private static final boolean REPORTING_ENABLED;
    private static final String PROJECT_KEY;
    private static final String HOST;
    private static final String TOKEN;
    private static final String RUN_DISPLAY_NAME;
    private static final String RUN_BUILD;
    private static final String RUN_ENVIRONMENT;
    private static final String RERUN_RUN_ID;
    private static final String SLACK_CHANNELS;

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

        SLACK_CHANNELS = configuration.getNotification().getSlack().getChannels();
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

    public static String getSlackChannels() {
        return SLACK_CHANNELS;
    }

}
