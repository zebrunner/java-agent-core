package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.exception.TestAgentException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlConfigurationProvider implements ConfigurationProvider {

    private final static String PROPERTY_SEPARATOR = ",";

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String PROJECT_KEY_PROPERTY = "reporting.project-key";

    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.access-token";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "reporting.run.display-name";
    private final static String RUN_BUILD_PROPERTY = "reporting.run.build";
    private final static String RUN_ENVIRONMENT_PROPERTY = "reporting.run.environment";

    private final static String RERUN_RUN_ID_PROPERTY = "reporting.rerun.run-id";

    private final static String NOTIFICATION_SLACK_CHANNELS_PROPERTY = "reporting.notification.slack.channels";

    private static final String[] DEFAULT_FILE_NAMES = {"agent.yaml", "agent.yml"};
    private static final Yaml YAML_MAPPER = new Yaml();

    @Override
    public ReportingConfiguration getConfiguration() {
        Map<String, Object> yamlProperties = loadYaml();

        String enabled = getProperty(yamlProperties, ENABLED_PROPERTY);
        String projectKey = getProperty(yamlProperties, PROJECT_KEY_PROPERTY);
        String hostname = getProperty(yamlProperties, HOSTNAME_PROPERTY);
        String accessToken = getProperty(yamlProperties, ACCESS_TOKEN_PROPERTY);
        String displayName = getProperty(yamlProperties, RUN_DISPLAY_NAME_PROPERTY);
        String build = getProperty(yamlProperties, RUN_BUILD_PROPERTY);
        String environment = getProperty(yamlProperties, RUN_ENVIRONMENT_PROPERTY);
        String runId = getProperty(yamlProperties, RERUN_RUN_ID_PROPERTY);
        String slackChannels = parseListToString(yamlProperties, NOTIFICATION_SLACK_CHANNELS_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("YAML configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .reportingEnabled(reportingEnabled)
                                     .projectKey(projectKey)
                                     .run(new ReportingConfiguration.RunConfiguration(displayName, build, environment))
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             new ReportingConfiguration.NotificationConfiguration.Slack(slackChannels)
                                     ))
                                     .build();
    }

    private static Map<String, Object> loadYaml() {
        for (String filename : DEFAULT_FILE_NAMES) {
            try (InputStream resource = YamlConfigurationProvider.class.getClassLoader()
                                                                       .getResourceAsStream(filename)) {
                if (resource != null) {
                    return YAML_MAPPER.load(resource);
                }
            } catch (IOException e) {
                throw new TestAgentException("Unable to load agent configuration from YAML file");
            }
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static String getProperty(Map<String, Object> yamlProperties, String key) {
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

    /**
     *  Remove brackets and spaces from list-provided property.
     *
     * <p>
     *     Get yaml properties map and find value by full path.
     *     Use substring(1, propertiesAsString.length() - 1) cause of getting raw list as [item1, item2, ..., itemn]
     *     then split by separator and remove additional spaces.
     *
     *     If no value with path was found, return null for next agent's processing.
     * </p>
     *
     * @param properties yaml properties map
     * @param path full path to map's value. Has next format: key1.key2.key3...
     * @return null if no property was found or joined string without brackets and spaces in following format: item1,item2,...,itemn
     */
    private String parseListToString(Map<String, Object> properties, String path) {
        String propertyListAsString = getProperty(properties, path);

        if (propertyListAsString == null) {
            return null;
        }

        String bracketsRegex = "\\[(.*?)]";
        if (propertyListAsString.matches(bracketsRegex)) {
            return Arrays.stream(propertyListAsString.substring(1, propertyListAsString.length() - 1).split(PROPERTY_SEPARATOR))
                         .map(String::trim)
                         .filter(channel -> !channel.isEmpty())
                         .collect(Collectors.joining(PROPERTY_SEPARATOR));
        } else {
            return propertyListAsString.trim();
        }
    }

}
