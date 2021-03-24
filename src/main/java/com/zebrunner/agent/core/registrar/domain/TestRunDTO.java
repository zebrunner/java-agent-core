package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRunDTO {

    private Long id;
    private String uuid;
    private String name;
    private String milestoneIdOrName;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private String framework;
    private Config config;
    private JenkinsContext jenkinsContext;
    private CiContextDTO ciContext;
    private Set<NotificationTargetDTO> notificationTargets;
    private MetaData _metaData;

    @Value
    public static class MetaData {

        List<String> warningMessages;

    }

    @Value
    public static class Config {

        String environment;
        String build;

    }

    @Value
    public static class JenkinsContext {

        String jobUrl;
        Integer jobNumber;
        String parentJobUrl;
        Integer parentJobNumber;

    }

}
