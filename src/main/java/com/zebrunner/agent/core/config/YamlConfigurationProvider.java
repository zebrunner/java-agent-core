package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class YamlConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.access-token";
    private final static String RUN_ID_PROPERTY = "reporting.rerun.run-id";

    private static final String[] DEFAULT_FILE_NAMES = {"agent.yaml", "agent.yml"};
    private static final Yaml YAML_MAPPER = new Yaml();

    private static Map<String, Object> yamlProperties;

    @Override
    public ReportingConfiguration getConfiguration() {
        if (yamlProperties == null) {
            yamlProperties = loadYaml();
        }

        String enabled = getProperty(ENABLED_PROPERTY);
        String hostname = getProperty(HOSTNAME_PROPERTY);
        String accessToken = getProperty(ACCESS_TOKEN_PROPERTY);
        String runId = getProperty(RUN_ID_PROPERTY);


        boolean enabledIsBoolean = enabled == null
                || String.valueOf(true).equalsIgnoreCase(enabled)
                || String.valueOf(false).equalsIgnoreCase(enabled);
        if (!enabledIsBoolean) {
            throw new TestAgentException("YAML configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .enabled(reportingEnabled)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId)).build();
    }

    private static Map<String, Object> loadYaml() {
        Map<String, Object> properties = null;
        for (String filename : DEFAULT_FILE_NAMES) {
            try (InputStream resource = YamlConfigurationProvider.class.getClassLoader().getResourceAsStream(filename)) {
                if (resource != null) {
                    properties = YAML_MAPPER.load(resource);
                    break;
                }
            } catch (IOException e) {
                throw new TestAgentException("Unable to load agent configuration from YAML file");
            }
        }
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static String getProperty(String key) {
        String result = null;
        String[] keySlices = key.split("\\.");

        Map<String, Object> slice = new HashMap<>(yamlProperties);
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

}
