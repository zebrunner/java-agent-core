package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Value;

import java.util.Map;

@Value
public class SessionStartDescriptor {

    String sessionId;
    Map<String, Object> capabilities;
    Map<String, Object> desiredCapabilities;

}
