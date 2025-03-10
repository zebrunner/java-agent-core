package com.zebrunner.agent.core.registrar.client.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.zebrunner.agent.core.registrar.domain.TestSession;

@Data
@Accessors(chain = true)
public class StartTestSessionRequest {

    private String sessionId;
    private Instant initiatedAt;
    private Instant startedAt;

    private TestSession.Status status;
    private String failureReason;

    private Object desiredCapabilities;
    private Object capabilities;

    private Set<Long> testIds = new HashSet<>();

}
