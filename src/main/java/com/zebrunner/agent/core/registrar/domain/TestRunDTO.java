package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRunDTO {

    private Long id;
    private String uuid;
    private String name;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private String framework;
    private Config config;
    private LaunchContext launchContext;
    private JenkinsContext jenkinsContext;

    @Value
    public static class Config {

        String environment;
        String build;

    }

    @Value
    public static class LaunchContext {

        String jobNumber;
        String upstreamJobNumber;

    }

    @Value
    public static class JenkinsContext {

        String jobUrl;
        Integer jobNumber;
        String parentJobUrl;
        Integer parentJobNumber;

    }

}
