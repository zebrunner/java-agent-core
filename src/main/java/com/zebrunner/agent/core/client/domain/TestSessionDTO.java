package com.zebrunner.agent.core.client.domain;

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
    private Instant startedAt;
    private Instant endedAt;
    private Object desiredCapabilities;
    private Object capabilities;
    @Builder.Default
    private Set<Long> testIds = new HashSet<>();

}
