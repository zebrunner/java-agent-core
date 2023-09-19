package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.annotation.SystemProperty;

import java.util.Arrays;
import java.util.List;

public class SystemPropertiesConfigurationProvider extends AnnotationDrivenConfigurationProvider<SystemProperty> {

    public SystemPropertiesConfigurationProvider() {
        super(SystemProperty.class);
    }

    @Override
    protected List<String> getConfigurationFieldKeys(SystemProperty annotation) {
        return Arrays.asList(annotation.value());
    }

    @Override
    protected String getConfigurationFieldValue(String key) {
        return System.getProperty(key);
    }

}
