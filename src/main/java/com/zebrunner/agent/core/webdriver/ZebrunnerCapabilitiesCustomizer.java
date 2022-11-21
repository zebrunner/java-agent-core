package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.registrar.domain.ObjectMapperImpl;
import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

@Slf4j
public class ZebrunnerCapabilitiesCustomizer implements CapabilitiesCustomizer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperImpl();
    private static final GenericType<Map<String, String>> CAPABILITIES_TYPE = new GenericType<>() {
    };

    @Override
    public Capabilities customize(Capabilities capabilities) {
        String zebrunnerCapabilities = System.getenv("ZEBRUNNER_CAPABILITIES");

        if (zebrunnerCapabilities != null) {
            log.debug("Capabilities will be modified with the values provided from Zebrunner.");

            Map<String, String> capabilitiesMap = OBJECT_MAPPER.readValue(zebrunnerCapabilities, CAPABILITIES_TYPE);
            return capabilities.merge(new DesiredCapabilities(capabilitiesMap));
        }

        return capabilities;
    }

}
