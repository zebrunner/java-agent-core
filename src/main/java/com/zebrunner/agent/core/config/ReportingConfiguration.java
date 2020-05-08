package com.zebrunner.agent.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ReportingConfiguration {

    private Boolean enabled;
    private ServerConfiguration server;
    private RerunConfiguration rerun;

    public Boolean isEnabled() {
        return enabled == null || enabled;
    }

    @Builder
    public ReportingConfiguration(Boolean enabled, ServerConfiguration server, RerunConfiguration rerun) {
        this.enabled = enabled;
        this.server = server;
        this.rerun = rerun;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServerConfiguration {

        private String hostname;
        private String accessToken;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RerunConfiguration {

        private String runId;

    }

}
