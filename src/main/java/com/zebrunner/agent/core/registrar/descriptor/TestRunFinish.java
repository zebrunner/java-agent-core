package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Value;

import java.time.Instant;

@Value
public class TestRunFinish {

    Instant endedAt;

}
