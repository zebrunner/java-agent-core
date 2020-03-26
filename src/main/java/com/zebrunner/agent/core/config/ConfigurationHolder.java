package com.zebrunner.agent.core.config;

public class ConfigurationHolder {

    private static final Configuration configuration;

    private static final String host;
    private static final String token;
    private static final String runId;

    static {
        ConfigurationProvider configurationProvider = DefaultConfigurationProviderChain.getInstance();
        configuration = configurationProvider.getConfiguration();

        host = configuration.getHostname();
        token = configuration.getAccessToken();
        runId = configuration.getRunId();
    }

    public static String getHost() {
        return host;
    }

    public static String getToken() {
        return token;
    }

    public static String getRunId() {
        return runId;
    }
}
