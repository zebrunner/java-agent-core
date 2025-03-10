package com.zebrunner.agent.core.registrar.domain;

import lombok.Data;

import java.time.Instant;

@Data
public class RunContextTestSummary {

    private Long id;
    private String name;
    private String correlationData;
    private Status status;
    private Instant startedAt;
    private Instant endedAt;

}
