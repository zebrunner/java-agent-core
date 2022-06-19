package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.zebrunner.agent.core.config.ConfigurationUtils.parseLong;

public class YamlConfigurationProvider implements ConfigurationProvider {

    private final static String ENABLED_PROPERTY = "reporting.enabled";
    private final static String PROJECT_KEY_PROPERTY = "reporting.project-key";

    private final static String HOSTNAME_PROPERTY = "reporting.server.hostname";
    private final static String ACCESS_TOKEN_PROPERTY = "reporting.server.access-token";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "reporting.run.display-name";
    private final static String RUN_BUILD_PROPERTY = "reporting.run.build";
    private final static String RUN_ENVIRONMENT_PROPERTY = "reporting.run.environment";
    private final static String RUN_CONTEXT_PROPERTY = "reporting.run.context";
    private final static String RUN_RETRY_KNOWN_ISSUES_PROPERTY = "reporting.run.retry-known-issues";
    private final static String RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_PROPERTY = "reporting.run.substitute-remote-web-drivers";
    private final static String RUN_TEST_CASE_STATUS_ON_PASS_PROPERTY = "reporting.run.test-case-status.on-pass";
    private final static String RUN_TEST_CASE_STATUS_ON_FAIL_PROPERTY = "reporting.run.test-case-status.on-fail";
    private final static String RUN_TEST_CASE_STATUS_ON_SKIP_PROPERTY = "reporting.run.test-case-status.on-skip";

    private static final String NOTIFICATION_NOTIFY_ON_EACH_FAILURE_VARIABLE = "reporting.notification.notify-on-each-failure";
    private final static String NOTIFICATION_SLACK_CHANNELS_PROPERTY = "reporting.notification.slack-channels";
    private final static String NOTIFICATION_MS_TEAMS_CHANNELS_PROPERTY = "reporting.notification.ms-teams-channels";
    private final static String NOTIFICATION_EMAILS_PROPERTY = "reporting.notification.emails";

    private final static String MILESTONE_ID_PROPERTY = "reporting.milestone.id";
    private final static String MILESTONE_NAME_PROPERTY = "reporting.milestone.name";

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
        String runContext = getProperty(yamlProperties, RUN_CONTEXT_PROPERTY);
        Boolean runRetryKnownIssues = ConfigurationUtils.parseBoolean(getProperty(yamlProperties, RUN_RETRY_KNOWN_ISSUES_PROPERTY));
        Boolean substituteRemoteWebDrivers = ConfigurationUtils.parseBoolean(getProperty(yamlProperties, RUN_SUBSTITUTE_REMOTE_WEB_DRIVERS_PROPERTY));
        String testCaseStatusOnPass = getProperty(yamlProperties, RUN_TEST_CASE_STATUS_ON_PASS_PROPERTY);
        String testCaseStatusOnFail = getProperty(yamlProperties, RUN_TEST_CASE_STATUS_ON_FAIL_PROPERTY);
        String testCaseStatusOnSkip = getProperty(yamlProperties, RUN_TEST_CASE_STATUS_ON_SKIP_PROPERTY);

        Boolean notifyOnEachFailure = ConfigurationUtils.parseBoolean(System.getenv(NOTIFICATION_NOTIFY_ON_EACH_FAILURE_VARIABLE));
        String slackChannels = getProperty(yamlProperties, NOTIFICATION_SLACK_CHANNELS_PROPERTY);
        String msTeamsChannels = getProperty(yamlProperties, NOTIFICATION_MS_TEAMS_CHANNELS_PROPERTY);
        String emails = getProperty(yamlProperties, NOTIFICATION_EMAILS_PROPERTY);

        Long milestoneId = parseLong(getProperty(yamlProperties, MILESTONE_ID_PROPERTY));
        String milestoneName = getProperty(yamlProperties, MILESTONE_NAME_PROPERTY);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("YAML configuration is malformed");
        }

        return ReportingConfiguration.builder()
                                     .reportingEnabled(ConfigurationUtils.parseBoolean(enabled))
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(
                                             hostname, accessToken
                                     ))
                                     .run(new ReportingConfiguration.RunConfiguration(
                                             displayName, build, environment, runContext, runRetryKnownIssues, substituteRemoteWebDrivers,
                                             new ReportingConfiguration.RunConfiguration.TestCaseStatus(
                                                     testCaseStatusOnPass, testCaseStatusOnFail, testCaseStatusOnSkip
                                             )
                                     ))
                                     .milestone(new ReportingConfiguration.MilestoneConfiguration(
                                             milestoneId, milestoneName
                                     ))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             notifyOnEachFailure, slackChannels, msTeamsChannels, emails
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

}
