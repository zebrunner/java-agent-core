package com.zebrunner.agent.core.registrar.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.zebrunner.agent.core.registrar.client.response.StartTestSessionResponse;
import com.zebrunner.agent.core.registrar.descriptor.SessionStart;

@Getter
@Accessors(chain = true)
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestSession {

    private Long id;

    private String sessionId;
    private Instant initiatedAt;
    private Instant startedAt;

    private Status status;
    private String failureReason;

    private Object desiredCapabilities;
    private Object capabilities;

    private Set<Long> testIds = new HashSet<>();

    public enum Status {

        RUNNING,
        COMPLETED,
        FAILED

    }

    public static TestSession of(StartTestSessionResponse startResponse, SessionStart sessionStart) {
        return new TestSession().setId(startResponse.getId())

                                .setSessionId(startResponse.getSessionId())
                                .setInitiatedAt(startResponse.getInitiatedAt())
                                .setStartedAt(startResponse.getStartedAt())

                                .setStatus(startResponse.getStatus())
                                .setFailureReason(startResponse.getFailureReason())

                                .setDesiredCapabilities(sessionStart.getDesiredCapabilities())
                                .setCapabilities(sessionStart.getCapabilities());
    }

}
