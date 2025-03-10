package com.zebrunner.agent.core.registrar.client.response;

import lombok.Data;
import lombok.Value;

import java.time.Instant;

@Data
public class StartTestRunResponse {

    private Long id;

    private String uuid;
    private String name;
    private String framework;
    private Instant startedAt;

    private Config config;

    @Value
    public static class Config {

        String environment;
        String build;

    }

}
