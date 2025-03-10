package com.zebrunner.agent.core.registrar.client.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class StartHeadlessTestRequest {

    private String name;
    private String correlationData;
    private Instant startedAt;

}
