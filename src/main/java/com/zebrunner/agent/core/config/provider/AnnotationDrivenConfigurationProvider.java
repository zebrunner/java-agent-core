package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.config.annotation.Configuration;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public abstract class AnnotationDrivenConfigurationProvider<T extends Annotation> implements ConfigurationProvider {

    private final Class<T> annotationClass;

    protected AnnotationDrivenConfigurationProvider(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public ReportingConfiguration getConfiguration() {
        ReportingConfiguration configuration = new ReportingConfiguration();

        this.beforePropertiesSet();
        this.setProperties(configuration);
        this.afterPropertiesSet();

        return configuration;
    }

    private void setProperties(Object configuration) {
        for (Field field : configuration.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(Configuration.class)) {
                Object nestedConfiguration = this.createNewInstanceOf(field);

                this.setProperties(nestedConfiguration);
                this.setFieldValue(configuration, field, nestedConfiguration);

                continue;
            }

            T annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                this.getConfigurationFieldKeys(annotation)
                    .stream()

                    .map(this::getConfigurationFieldValue)
                    .filter(Objects::nonNull)

                    .findFirst()
                    .map(fieldValue -> this.parse(field, fieldValue))

                    .ifPresent(fieldValue -> this.setFieldValue(configuration, field, fieldValue));
            }

        }
    }

    @SneakyThrows
    private Object createNewInstanceOf(Field field) {
        try {
            Constructor<?> noArgsConstructor = field.getType().getDeclaredConstructor();

            noArgsConstructor.setAccessible(true);
            Object newInstance = noArgsConstructor.newInstance();
            noArgsConstructor.setAccessible(false);

            return newInstance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("No-args constructor doesn't exist for type: %s", field.getType().getName()));
        }
    }

    private Object parse(Field field, String value) {
        if (value == null) {
            return null;
        }

        Object parsedValue = value;
        if (field.getType() == Boolean.class) {
            parsedValue = ConfigurationUtils.parseBoolean(value);
        } else if (field.getType() == Long.class) {
            parsedValue = ConfigurationUtils.parseLong(value);
        }

        return parsedValue;
    }

    @SneakyThrows
    private void setFieldValue(Object fieldHolder, Field field, Object value) {
        field.setAccessible(true);
        field.set(fieldHolder, value);
        field.setAccessible(false);
    }

    protected void beforePropertiesSet() {
    }

    protected void afterPropertiesSet() {
    }

    protected abstract List<String> getConfigurationFieldKeys(T annotation);

    protected abstract String getConfigurationFieldValue(String key);

}
