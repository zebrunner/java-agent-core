package com.zebrunner.agent.core.listener.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.openqa.selenium.Capabilities;

@SuperBuilder
@Getter
public class StartSessionContext extends WebDriverContext {

    private Capabilities desiredCapabilities;

}
