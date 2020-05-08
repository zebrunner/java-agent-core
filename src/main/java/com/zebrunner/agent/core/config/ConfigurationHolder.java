package com.zebrunner.agent.core.config;

public class ConfigurationHolder {

    private static String HOST;
    private static String TOKEN;
    private static boolean ENABLED;
    private static String RUN_ID;

    static {
        ConfigurationProvider configurationProvider = DefaultConfigurationProviderChain.getInstance();
        ReportingConfiguration configuration = configurationProvider.getConfiguration();

        ENABLED = configuration.isEnabled();

        HOST = configuration.getServer().getHostname();
        TOKEN = configuration.getServer().getAccessToken();
        RUN_ID = configuration.getRerun().getRunId();
    }

    public static String getHost() {
        return HOST;
    }

    public static String getToken() {
        return TOKEN;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static String getRunId() {
        return RUN_ID;
    }

}
