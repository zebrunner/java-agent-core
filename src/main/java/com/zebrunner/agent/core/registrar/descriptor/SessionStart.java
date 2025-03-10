package com.zebrunner.agent.core.registrar.descriptor;

import com.zebrunner.agent.core.registrar.domain.TestSession;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionStart {

    private final Instant initiatedAt = Instant.now();
    private final Map<String, Object> desiredCapabilities;

    private TestSession.Status status;

    private Instant startedAt;
    private String sessionId;
    private Map<String, Object> capabilities;
    private String failureReason;

    public static SessionStart initiatedWith(Map<String, Object> desiredCapabilities) {
        return new SessionStart(desiredCapabilities);
    }

    public void successfullyStartedWith(String sessionId, Map<String, Object> capabilities) {
        this.status = TestSession.Status.RUNNING;
        this.startedAt = Instant.now();
        this.sessionId = sessionId;
        this.capabilities = capabilities;
    }

    public void failedToStart(String failureReason) {
        this.status = TestSession.Status.FAILED;
        this.failureReason = failureReason;
    }

}
