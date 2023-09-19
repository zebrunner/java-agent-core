package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.annotation.YamlProperty;
import com.zebrunner.agent.core.exception.TestAgentException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlConfigurationProvider extends AnnotationDrivenConfigurationProvider<YamlProperty> {

    private static final String[] DEFAULT_FILE_NAMES = {"agent.yaml", "agent.yml"};
    private static final Yaml YAML_MAPPER = new Yaml();

    private Map<String, Object> properties;

    public YamlConfigurationProvider() {
        super(YamlProperty.class);
    }

    @Override
    protected List<String> getConfigurationFieldKeys(YamlProperty annotation) {
        return Arrays.asList(annotation.value());
    }

    @Override
    protected String getConfigurationFieldValue(String key) {
        return this.getProperty(key);
    }

    @SuppressWarnings("unchecked")
    private String getProperty(String key) {
        String result = null;

        String[] keySlices = key.split("\\.");

        Map<String, Object> slice = new HashMap<>(this.properties);
        for (int i = 0; i < keySlices.length; i++) {
            String keySlice = keySlices[i];
            Object sliceValue = slice.get(keySlice);
            if (sliceValue != null) {
                if (sliceValue instanceof Map) {
                    slice = (Map<String, Object>) sliceValue;
                } else {
                    if (i == keySlices.length - 1) {
                        result = sliceValue.toString();
                    }
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    @Override
    protected void beforePropertiesSet() {
        for (String filename : DEFAULT_FILE_NAMES) {
            try (InputStream resource = YamlConfigurationProvider.class.getClassLoader().getResourceAsStream(filename)) {
                if (resource != null) {
                    this.properties = YAML_MAPPER.load(resource);
                }
            } catch (IOException e) {
                throw new TestAgentException("Unable to load agent configuration from YAML file");
            }
        }

        if (this.properties == null) {
            this.properties = Collections.emptyMap();
        }
    }

    @Override
    protected void afterPropertiesSet() {
        this.properties.clear();
        this.properties = null;
    }

}
