package com.zebrunner.agent.core.webdriver;

import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

import com.zebrunner.agent.core.registrar.client.ObjectMapperImpl;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteWebDriverFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperImpl();
    private static final GenericType<Map<String, String>> CAPABILITIES_TYPE = new GenericType<Map<String, String>>() {
    };

    @SneakyThrows
    public static URL getSeleniumHubUrl() {
        String hubUrl = System.getenv("ZEBRUNNER_HUB_URL");
        return hubUrl != null ? new URL(hubUrl) : null;
    }

    private static URL getMandatorySeleniumHubUrl() {
        return Optional.ofNullable(RemoteWebDriverFactory.getSeleniumHubUrl())
                       .orElseThrow(() -> new RuntimeException("Zebrunner didn't provide a selenium hub url."));
    }

    public static Capabilities getCapabilities() {
        String capabilities = System.getenv("ZEBRUNNER_CAPABILITIES");

        Capabilities desiredCapabilities = new DesiredCapabilities();
        if (capabilities != null) {
            Map<String, String> capabilitiesMap = OBJECT_MAPPER.readValue(capabilities, CAPABILITIES_TYPE);
            desiredCapabilities = new DesiredCapabilities(capabilitiesMap);
        }

        return desiredCapabilities;
    }

    public static RemoteWebDriver getDriver() {
        return new RemoteWebDriver(RemoteWebDriverFactory.getMandatorySeleniumHubUrl(), RemoteWebDriverFactory.getCapabilities());
    }

    public static RemoteWebDriver getDriver(Capabilities capabilities) {
        capabilities = capabilities.merge(RemoteWebDriverFactory.getCapabilities());
        return new RemoteWebDriver(RemoteWebDriverFactory.getMandatorySeleniumHubUrl(), capabilities);
    }

    public static RemoteWebDriver getDriver(Map<String, ?> capabilitiesMap) {
        return RemoteWebDriverFactory.getDriver(new DesiredCapabilities(capabilitiesMap));
    }

}
