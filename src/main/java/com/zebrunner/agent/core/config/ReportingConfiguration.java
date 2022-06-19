package com.zebrunner.agent.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReportingConfiguration {

    private Boolean reportingEnabled;
    private String projectKey;
    private ServerConfiguration server;
    private RunConfiguration run;
    private MilestoneConfiguration milestone;
    private NotificationConfiguration notification;

    public boolean isReportingEnabled() {
        return reportingEnabled != null && reportingEnabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneConfiguration {

        private Long id;
        private String name;

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
        private String context;
        private Boolean retryKnownIssues;
        private Boolean substituteRemoteWebDrivers;
        private TestCaseStatus testCaseStatus = new TestCaseStatus();

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TestCaseStatus {

            private String onPass;
            private String onFail;
            private String onSkip;

        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfiguration {

        private Boolean notifyOnEachFailure;
        private String slackChannels;
        private String msTeamsChannels;
        private String emails;

    }

}
