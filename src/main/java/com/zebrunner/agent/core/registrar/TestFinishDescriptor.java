package com.zebrunner.agent.core.registrar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;

/**
 * Descriptor of test finish event. Captures resulting test data, such as its result/status, end time
 * and optional reason explaining its status.
 */
@Getter
@AllArgsConstructor
@ToString
public class TestFinishDescriptor {

    private final Status status;
    private OffsetDateTime endedAt;
    private String statusReason;

    public TestFinishDescriptor(Status status) {
        this(status, OffsetDateTime.now());
    }

    public TestFinishDescriptor(Status status, OffsetDateTime endedAt) {
        this(status, endedAt, null);
    }

}
