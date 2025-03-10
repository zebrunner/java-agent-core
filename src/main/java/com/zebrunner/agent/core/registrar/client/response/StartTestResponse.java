package com.zebrunner.agent.core.registrar.client.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.zebrunner.agent.core.registrar.domain.ArtifactReference;
import com.zebrunner.agent.core.registrar.domain.Label;

@Data
public class StartTestResponse {

    private Long id;

    private String name;
    private String correlationData;

    private String className;
    private String methodName;
    private Integer argumentsIndex;
    private String maintainer;

    private Instant startedAt;
    private Instant endedAt;

    private List<Label> labels;
    private List<ArtifactReference> artifactReferences;
    private Set<String> testGroups;

}
