package com.zebrunner.agent.core.registrar.descriptor;

import com.zebrunner.agent.core.registrar.domain.TestSessionDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionStartDescriptor {

    private final Instant initiatedAt = Instant.now();
    private final Map<String, Object> desiredCapabilities;

    private TestSessionDTO.Status status;

    private Instant startedAt;
    private String sessionId;
    private Map<String, Object> capabilities;

    public static SessionStartDescriptor initiatedWith(Map<String, Object> desiredCapabilities) {
        return new SessionStartDescriptor(desiredCapabilities);
    }

    public void successfullyStartedWith(String sessionId, Map<String, Object> capabilities) {
        this.status = TestSessionDTO.Status.RUNNING;
        this.startedAt = Instant.now();
        this.sessionId = sessionId;
        this.capabilities = capabilities;
    }

    public void failedToStart() {
        this.status = TestSessionDTO.Status.FAILED;
    }

}
