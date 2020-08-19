package com.zebrunner.agent.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
    private RerunConfiguration rerun;

    public boolean isReportingEnabled() {
        return reportingEnabled == null || reportingEnabled;
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

}
