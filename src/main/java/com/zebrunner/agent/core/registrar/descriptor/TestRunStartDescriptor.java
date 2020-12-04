package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class TestRunStartDescriptor {

    String name;
    String framework;
    OffsetDateTime startedAt;
    String fileName;

}
