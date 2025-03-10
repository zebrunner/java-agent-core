package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.registrar.client.ObjectMapperImpl;
import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

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
        return Optional.ofNullable(getSeleniumHubUrl())
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
        return new RemoteWebDriver(getMandatorySeleniumHubUrl(), getCapabilities());
    }

    public static RemoteWebDriver getDriver(Capabilities capabilities) {
        capabilities = capabilities.merge(getCapabilities());
        return new RemoteWebDriver(getMandatorySeleniumHubUrl(), capabilities);
    }

    public static RemoteWebDriver getDriver(Map<String, ?> capabilitiesMap) {
        Capabilities capabilities = new DesiredCapabilities(capabilitiesMap);
        capabilities = capabilities.merge(getCapabilities());
        return new RemoteWebDriver(getMandatorySeleniumHubUrl(), capabilities);
    }

}
