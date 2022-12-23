package com.zebrunner.agent.core.webdriver;

import org.openqa.selenium.Capabilities;

public interface CapabilitiesCustomizer {

    Capabilities customize(Capabilities capabilities);

}
