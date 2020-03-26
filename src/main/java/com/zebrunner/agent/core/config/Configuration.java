package com.zebrunner.agent.core.config;

import lombok.Data;

@Data
public class Configuration {

    private final String hostname;
    private final String accessToken;
    private final String runId;

}
