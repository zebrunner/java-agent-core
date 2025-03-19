package com.zebrunner.agent.core.registrar.client.request;

import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

import com.zebrunner.agent.core.registrar.domain.CiContext;
import com.zebrunner.agent.core.registrar.domain.NotificationTarget;

@Data
@Accessors(chain = true)
public class StartTestRunRequest {

    private String uuid;
    private String name;
    private String framework;
    private Instant startedAt;

    private Config config;
    private Milestone milestone;
    private CiContext ciContext;
    private Notifications notifications;

    @Value
    public static class Milestone {

        Long id;
        String name;

    }

    @Value
    public static class Config {

        String environment;
        String build;
        boolean treatSkipsAsFailures;

    }

    @Value
    public static class Notifications {

        boolean enabled;
        List<NotificationTarget> targets;
        boolean notifyOnEachFailure;

    }

}
