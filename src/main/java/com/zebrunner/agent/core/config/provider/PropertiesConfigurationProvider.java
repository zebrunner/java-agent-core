package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.annotation.PropertiesFileProperty;
import com.zebrunner.agent.core.exception.TestAgentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesConfigurationProvider extends AnnotationDrivenConfigurationProvider<PropertiesFileProperty> {

    private static final String DEFAULT_FILE_NAME = "agent.properties";

    private Properties properties;

    public PropertiesConfigurationProvider() {
        super(PropertiesFileProperty.class);
    }

    @Override
    protected List<String> getConfigurationFieldKeys(PropertiesFileProperty annotation) {
        return Arrays.asList(annotation.value());
    }

    @Override
    protected String getConfigurationFieldValue(String key) {
        return this.properties.getProperty(key);
    }

    @Override
    protected void beforePropertiesSet() {
        this.properties = new Properties();
        try (InputStream resource = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME)) {
            if (resource != null) {
                this.properties.load(resource);
            }
        } catch (IOException e) {
            throw new TestAgentException("Unable to load agent configuration from properties file");
        }
    }

    @Override
    protected void afterPropertiesSet() {
        this.properties.clear();
        this.properties = null;
    }

}
