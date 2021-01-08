package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.OffsetDateTime;

/**
 * Descriptor of test finish event. Captures resulting test data, such as its result/status, end time
 * and optional reason explaining its status.
 */
@Value
@AllArgsConstructor
public class TestFinishDescriptor {

    Status status;
    OffsetDateTime endedAt;
    String statusReason;

    public TestFinishDescriptor(Status status) {
        this(status, OffsetDateTime.now());
    }

    public TestFinishDescriptor(Status status, OffsetDateTime endedAt) {
        this(status, endedAt, null);
    }

}
