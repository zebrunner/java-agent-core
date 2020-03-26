package com.zebrunner.agent.core.config;

import java.util.List;

public class DefaultConfigurationProviderChain extends ConfigurationProviderChain {

    private static final DefaultConfigurationProviderChain INSTANCE = new DefaultConfigurationProviderChain();

    public DefaultConfigurationProviderChain() {
        super(List.of(new EnvironmentConfigurationProvider(), new SystemPropertiesConfigurationProvider(), new PropertiesFileConfigurationProvider()));
    }

    public DefaultConfigurationProviderChain(List<? extends ConfigurationProvider> credentialsProviders) {
        super(credentialsProviders);
    }

    public static DefaultConfigurationProviderChain getInstance() {
        return INSTANCE;
    }

}
