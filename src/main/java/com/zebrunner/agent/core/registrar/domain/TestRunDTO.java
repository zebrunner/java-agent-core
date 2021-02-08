package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.time.OffsetDateTime;
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
    private LaunchContextDTO launchContext;
    private Set<NotificationDTO> notifications;

    @Value
    public static class LaunchContextDTO {

        String jobNumber;
        String upstreamJobNumber;

    }

    @Getter
    public static class Config {

        private final String environment;
        private final String build;

        public Config(String environment, String build) {
            this.environment = environment;
            this.build = build;
        }

    }

    @Value
    public static class NotificationDTO {

        String key;
        String value;

    }

}
