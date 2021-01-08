package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Value;
import org.openqa.selenium.Capabilities;

@Value
public class SessionStartDescriptor {

    String sessionId;
    Capabilities capabilities;
    Capabilities desiredCapabilities;

}
