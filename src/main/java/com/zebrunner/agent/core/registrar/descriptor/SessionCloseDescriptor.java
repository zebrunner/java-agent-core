package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Value;

import java.time.Instant;

@Value
public class SessionCloseDescriptor {

    String sessionId;
    Instant endedAt;

    public static SessionCloseDescriptor of(String sessionId) {
        return new SessionCloseDescriptor(sessionId, Instant.now());
    }

}
