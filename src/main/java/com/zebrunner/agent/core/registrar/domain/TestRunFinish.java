package com.zebrunner.agent.core.registrar.domain;

import lombok.Value;

import java.time.Instant;

@Value
public class TestRunFinish {

    Instant endedAt;

}
