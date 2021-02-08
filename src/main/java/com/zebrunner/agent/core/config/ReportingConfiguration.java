package com.zebrunner.agent.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class ReportingConfiguration {

    private Boolean reportingEnabled;
    private String projectKey;
    private ServerConfiguration server;
    private RunConfiguration run;
    private RerunConfiguration rerun;
    private NotificationConfiguration notification;

    public boolean isReportingEnabled() {
        return reportingEnabled != null && reportingEnabled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerConfiguration {

        private String hostname;
        private String accessToken;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunConfiguration {

        private String displayName;
        private String build;
        private String environment;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RerunConfiguration {

        private String runId;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfiguration {

        private Set<String> slackChannels;
        private Set<String> microsoftTeamsChannels;
        private Set<String> emails;

    }

}
