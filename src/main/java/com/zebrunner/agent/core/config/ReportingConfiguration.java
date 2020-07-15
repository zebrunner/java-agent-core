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

    private Boolean enabled;
    private String projectKey;
    private ServerConfiguration server;
    private RerunConfiguration rerun;

    public Boolean isEnabled() {
        return enabled == null || enabled;
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
    public static class RerunConfiguration {

        private String runId;

    }

}
