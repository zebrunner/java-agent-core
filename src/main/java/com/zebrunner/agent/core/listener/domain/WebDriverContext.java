package com.zebrunner.agent.core.listener.domain;

import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.openqa.selenium.Capabilities;

@SuperBuilder
@Setter
public class WebDriverContext {

    private Capabilities actualCapabilities;
    private String sessionId;

}
