package com.zebrunner.agent.core.config;

public class ConfigurationHolder {

    private static final boolean ENABLED;
    private static final String PROJECT_KEY;
    private static final String HOST;
    private static final String TOKEN;
    private static final String RUN_ID;

    static {
        ConfigurationProvider configurationProvider = DefaultConfigurationProviderChain.getInstance();
        ReportingConfiguration configuration = configurationProvider.getConfiguration();

        ENABLED = configuration.isEnabled();
        PROJECT_KEY = configuration.getProjectKey();

        HOST = configuration.getServer().getHostname();
        TOKEN = configuration.getServer().getAccessToken();
        RUN_ID = configuration.getRerun().getRunId();
    }

    public static boolean isEnabled() {
        return ENABLED;
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

    public static String getRunId() {
        return RUN_ID;
    }

}
