package com.zebrunner.agent.core.config.provider;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    private final static String VALUE_SEPARATORS = "[,;]";

    private final static String ENABLED_VARIABLE = "REPORTING_ENABLED";
    private final static String PROJECT_KEY_VARIABLE = "REPORTING_PROJECT_KEY";

    private final static String HOSTNAME_VARIABLE = "REPORTING_SERVER_HOSTNAME";
    private final static String ACCESS_TOKEN_VARIABLE = "REPORTING_SERVER_ACCESS_TOKEN";

    private final static String RUN_DISPLAY_NAME_PROPERTY = "REPORTING_RUN_DISPLAY_NAME";
    private final static String RUN_BUILD_PROPERTY = "REPORTING_RUN_BUILD";
    private final static String RUN_ENVIRONMENT_PROPERTY = "REPORTING_RUN_ENVIRONMENT";

    private final static String RUN_ID_VARIABLE = "REPORTING_RERUN_RUN_ID";

    private final static String NOTIFICATION_SLACK_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_SLACK_CHANNELS";
    private final static String NOTIFICATION_MICROSOFT_TEAMS_CHANNELS_VARIABLE = "REPORTING_NOTIFICATION_MICROSOFT_TEAMS_CHANNELS";
    private final static String NOTIFICATION_EMAILS = "REPORTING_NOTIFICATION_EMAILS";

    @Override
    public ReportingConfiguration getConfiguration() {
        String enabled = System.getenv(ENABLED_VARIABLE);
        String projectKey = System.getenv(PROJECT_KEY_VARIABLE);
        String hostname = System.getenv(HOSTNAME_VARIABLE);
        String accessToken = System.getenv(ACCESS_TOKEN_VARIABLE);
        String displayName = System.getenv(RUN_DISPLAY_NAME_PROPERTY);
        String build = System.getenv(RUN_BUILD_PROPERTY);
        String environment = System.getenv(RUN_ENVIRONMENT_PROPERTY);
        String runId = System.getenv(RUN_ID_VARIABLE);
        Set<String> slackChannels = getEnvironmentValueAsSet(NOTIFICATION_SLACK_CHANNELS_VARIABLE, VALUE_SEPARATORS);
        Set<String> microsoftTeamsChannels = getEnvironmentValueAsSet(NOTIFICATION_MICROSOFT_TEAMS_CHANNELS_VARIABLE, VALUE_SEPARATORS);
        Set<String> emails = getEnvironmentValueAsSet(NOTIFICATION_EMAILS, VALUE_SEPARATORS);

        if (enabled != null && !"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
            throw new TestAgentException("Environment configuration is malformed, skipping");
        }

        Boolean reportingEnabled = enabled != null ? Boolean.parseBoolean(enabled) : null;
        return ReportingConfiguration.builder()
                                     .reportingEnabled(reportingEnabled)
                                     .projectKey(projectKey)
                                     .server(new ReportingConfiguration.ServerConfiguration(hostname, accessToken))
                                     .run(new ReportingConfiguration.RunConfiguration(displayName, build, environment))
                                     .rerun(new ReportingConfiguration.RerunConfiguration(runId))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(slackChannels, microsoftTeamsChannels, emails))
                                     .build();
    }

    private Set<String> getEnvironmentValueAsSet(String key, String separator) {
        String environmentListAsString = System.getenv(key);

        if (environmentListAsString == null) {
            return Set.of();
        }

        return Arrays.stream(environmentListAsString.split(separator))
                     .filter(channel -> !channel.isBlank())
                     .map(String::trim)
                     .collect(Collectors.toSet());
    }

}
