package com.zebrunner.agent.core.config;

public class ConfigurationHolder {

    private static final String HOST;
    private static final String TOKEN;
    private static final String RUN_ID;

    static {
        ConfigurationProvider configurationProvider = DefaultConfigurationProviderChain.getInstance();
        Configuration configuration = configurationProvider.getConfiguration();

        HOST = configuration.getHostname();
        TOKEN = configuration.getAccessToken();
        RUN_ID = configuration.getRunId();
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
