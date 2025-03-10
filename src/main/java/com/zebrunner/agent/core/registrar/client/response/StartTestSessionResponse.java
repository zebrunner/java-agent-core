package com.zebrunner.agent.core.registrar.client.response;

import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.zebrunner.agent.core.registrar.domain.TestSession;

@Data
public class StartTestSessionResponse {

    private Long id;

    private String sessionId;
    private Instant initiatedAt;
    private Instant startedAt;

    private TestSession.Status status;
    private String failureReason;

    private Set<Long> testIds = new HashSet<>();

}
