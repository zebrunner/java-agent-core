package com.zebrunner.agent.core.registrar.client.response;

import lombok.Data;

import java.time.Instant;

@Data
public class StartTestRunResponse {

    private Long id;

    private String uuid;
    private String name;
    private String framework;
    private Instant startedAt;

    private Config config = new Config();

    @Data
    public static class Config {

        private String environment;
        private String build;

    }

}
