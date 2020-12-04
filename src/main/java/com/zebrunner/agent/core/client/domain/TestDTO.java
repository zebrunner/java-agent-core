package com.zebrunner.agent.core.client.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDTO {

    private Long id;
    private String uuid;
    private String name;
    private String className;
    private String methodName;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private String maintainer;
    private String testCase;
    private Map<String, List<String>> labels;
    private List<ArtifactReferenceDTO> artifactReferences;
    private String result;
    private String reason;

}
