package com.zebrunner.agent.core.registrar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@ToString
public class TestRunStartDescriptor {

    private final String name;
    private final String framework;
    private final OffsetDateTime startedAt;
    private final String fileName;

}
