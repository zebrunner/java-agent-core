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
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private String framework;
    private Config config;
    private Milestone milestone;
    private JenkinsContext jenkinsContext;
    private CiContextDTO ciContext;
    private Notifications notifications;
    private Metadata metadata;

    @Value
    public static class Milestone {

        Long id;
        String name;

    }

    @Value
    public static class Metadata {

        List<String> warningMessages;

    }

    @Value
    public static class Config {

        String environment;
        String build;
        boolean treatSkipsAsFailures;

    }

    @Value
    public static class JenkinsContext {

        String jobUrl;
        Integer jobNumber;
        String parentJobUrl;
        Integer parentJobNumber;

    }

    @Value
    public static class Notifications {

        boolean enabled;
        Set<NotificationTargetDTO> targets;
        boolean notifyOnEachFailure;
        SummarySendingPolicy summarySendingPolicy;

    }

}
