package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.config.provider.EnvironmentConfigurationProvider;
import com.zebrunner.agent.core.config.provider.PropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.SystemPropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.YamlConfigurationProvider;

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
