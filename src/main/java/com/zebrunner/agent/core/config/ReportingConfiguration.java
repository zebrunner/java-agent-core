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
    private TcmConfiguration tcm;

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
        private Boolean treatSkipsAsFailures;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfiguration {

        private Boolean enabled;
        private Boolean notifyOnEachFailure;
        private String slackChannels;
        private String msTeamsChannels;
        private String emails;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TcmConfiguration {

        private TestCaseStatus testCaseStatus = new TestCaseStatus();
        private Zebrunner zebrunner = new Zebrunner();
        private TestRail testRail = new TestRail();
        private Xray xray = new Xray();
        private Zephyr zephyr = new Zephyr();

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TestCaseStatus {

            private String onPass;
            private String onFail;
            private String onSkip;

        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Zebrunner {

            private Boolean pushResults;
            private Boolean pushInRealTime;
            private String testRunId;

        }

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TestRail {

            private Boolean pushResults;
            private Boolean pushInRealTime;
            private String suiteId;
            private String runId;
            private Boolean includeAllTestCasesInNewRun;
            private String runName;
            private String milestoneName;
            private String assignee;

        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Xray {

            private Boolean pushResults;
            private Boolean pushInRealTime;
            private String executionKey;

        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Zephyr {

            private Boolean pushResults;
            private Boolean pushInRealTime;
            private String jiraProjectKey;
            private String testCycleKey;

        }

    }

}
