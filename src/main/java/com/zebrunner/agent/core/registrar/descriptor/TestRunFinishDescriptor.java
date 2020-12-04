package com.zebrunner.agent.core.registrar.descriptor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@ToString
public class TestRunFinishDescriptor {

    private final OffsetDateTime endedAt;

}
