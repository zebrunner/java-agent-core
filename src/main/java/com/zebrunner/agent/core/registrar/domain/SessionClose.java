package com.zebrunner.agent.core.registrar.domain;

import lombok.Value;

import java.time.Instant;

@Value
public class SessionClose {

    String sessionId;
    Instant endedAt;

    public static SessionClose of(String sessionId) {
        return new SessionClose(sessionId, Instant.now());
    }

}
