package com.zebrunner.agent.core.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class TestSessionDTO {

    private Long id;
    private String sessionId;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private Object desiredCapabilities;
    private Object capabilities;
    private Set<String> testRefs;

    @Builder
    public TestSessionDTO(Long id, String sessionId, OffsetDateTime startedAt, OffsetDateTime endedAt, Object desiredCapabilities, Object capabilities, Set<String> testRefs) {
        this.id = id;
        this.sessionId = sessionId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.desiredCapabilities = desiredCapabilities;
        this.capabilities = capabilities;
        this.testRefs = testRefs != null ? testRefs : new HashSet<>();
    }
}
