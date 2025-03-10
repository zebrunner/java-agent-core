package com.zebrunner.agent.core.registrar.client.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class CloseTestSessionRequest {

    private Instant endedAt;

}
