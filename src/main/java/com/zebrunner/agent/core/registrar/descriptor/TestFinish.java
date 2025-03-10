package com.zebrunner.agent.core.registrar.descriptor;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;

import com.zebrunner.agent.core.registrar.domain.Status;

/**
 * Descriptor of test finish event. Captures resulting test data, such as its result/status, end time
 * and optional reason explaining its status.
 */
@Value
@RequiredArgsConstructor
public class TestFinish {

    Status status;
    Instant endedAt;
    String statusReason;

    public TestFinish(Status status) {
        this(status, Instant.now());
    }

    public TestFinish(Status status, Instant endedAt) {
        this(status, endedAt, null);
    }

}
