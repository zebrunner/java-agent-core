package com.zebrunner.agent.core.registrar.descriptor;

import lombok.Value;
import org.openqa.selenium.Capabilities;

@Value
public class SessionCloseDescriptor {

    String sessionId;
    Capabilities capabilities;

}
