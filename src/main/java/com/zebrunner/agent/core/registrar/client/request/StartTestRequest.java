package com.zebrunner.agent.core.registrar.client.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

import com.zebrunner.agent.core.registrar.domain.Label;

@Data
@Accessors(chain = true)
public class StartTestRequest {

    private String name;
    private String correlationData;

    private String className;
    private String methodName;
    private Integer argumentsIndex;
    private String maintainer;

    private Instant startedAt;

    private List<Label> labels;
    private List<String> testGroups;

}
