package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;

import java.util.LinkedList;
import java.util.List;

public class ConfigurationProviderChain implements ConfigurationProvider {

    private final List<ConfigurationProvider> providers = new LinkedList<>();

    public ConfigurationProviderChain(List<? extends ConfigurationProvider> credentialsProviders) {
        if (credentialsProviders == null || credentialsProviders.size() == 0) {
            throw new IllegalArgumentException("No credential providers specified");
        }
        this.providers.addAll(credentialsProviders);
    }

    @Override
    public Configuration getConfiguration() {
        List<String> exceptionMessages = null;

        for (ConfigurationProvider provider : providers) {
            try {
                Configuration configuration = provider.getConfiguration();

                if (configuration != null) {
                    return configuration;
                }
            } catch (Exception e) {
                String providerMessage = provider + ": " + e.getMessage();
                if (exceptionMessages == null) {
                    exceptionMessages = new LinkedList<>();
                }
                exceptionMessages.add(providerMessage);
            }
        }

        throw new TestAgentException("Unable to load agent configuration: " + exceptionMessages);
    }

}
