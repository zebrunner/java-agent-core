package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSessionDTO {

    private Long id;
    private String sessionId;
    private Instant initiatedAt;
    private Instant startedAt;
    private Instant endedAt;
    private Status status;
    private Object desiredCapabilities;
    private Object capabilities;
    @Builder.Default
    private Set<Long> testIds = new HashSet<>();
    private String failureReason;

    public enum Status {

        RUNNING,
        COMPLETED,
        FAILED

    }

}
