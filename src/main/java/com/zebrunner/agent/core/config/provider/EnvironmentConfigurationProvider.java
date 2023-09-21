package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.annotation.EnvironmentVariable;

import java.util.List;

public class EnvironmentConfigurationProvider extends AnnotationDrivenConfigurationProvider<EnvironmentVariable> {

    public EnvironmentConfigurationProvider() {
        super(EnvironmentVariable.class);
    }

    @Override
    protected List<String> getConfigurationFieldKeys(EnvironmentVariable annotation) {
        return List.of(annotation.value());
    }

    @Override
    protected String getConfigurationFieldValue(String key) {
        return System.getenv(key);
    }

}
