package com.zebrunner.agent.core.config;

import java.util.Arrays;

public class DefaultConfigurationProviderChain extends ConfigurationProviderChain {

    private static final DefaultConfigurationProviderChain INSTANCE = new DefaultConfigurationProviderChain();

    public DefaultConfigurationProviderChain() {
        super(Arrays.asList(
                new EnvironmentConfigurationProvider(),
                new SystemPropertiesConfigurationProvider(),
                new YamlConfigurationProvider(),
                new PropertiesConfigurationProvider()
        ));
    }

    public static DefaultConfigurationProviderChain getInstance() {
        return INSTANCE;
    }

}
