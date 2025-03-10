package com.zebrunner.agent.core.webdriver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.zebrunner.agent.core.registrar.client.ObjectMapperImpl;
import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ZebrunnerCapabilitiesCustomizer implements CapabilitiesCustomizer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperImpl();
    private static final GenericType<Map<String, ?>> CAPABILITIES_TYPE = new GenericType<>() {
    };

    @Override
    public Capabilities customize(Capabilities originalCapabilities) {
        String serializedCapabilities = System.getenv("ZEBRUNNER_CAPABILITIES");
        if (serializedCapabilities == null) {
            return originalCapabilities;
        }

        JsonObject capabilities;
        try {
            capabilities = OBJECT_MAPPER.readValue(serializedCapabilities, JsonObject.class);
        } catch (JsonSyntaxException e) {
            log.warn("'ZEBRUNNER_CAPABILITIES' must have json object format. Skipping customization...");
            return originalCapabilities;
        }

        log.debug("Capabilities will be modified with the values provided from Zebrunner.");

        Set<String> keys = new HashSet<>(capabilities.keySet());
        for (String key : keys) {
            if (!key.contains(".")) {
                continue;
            }

            String[] keyParts = key.split("\\.");

            JsonObject capabilityNode = capabilities;
            for (int i = 0; i < keyParts.length - 1; i++) {
                String keyPart = keyParts[i];

                if (!capabilityNode.has(keyPart)) {
                    capabilityNode.add(keyPart, new JsonObject());
                }

                capabilityNode = capabilityNode.getAsJsonObject(keyPart);
            }

            JsonElement value = capabilities.remove(key);
            String lastKeyPart = keyParts[keyParts.length - 1];

            capabilityNode.add(lastKeyPart, value);
        }

        serializedCapabilities = capabilities.toString();
        Map<String, ?> capabilitiesMap = OBJECT_MAPPER.readValue(serializedCapabilities, CAPABILITIES_TYPE);

        return originalCapabilities.merge(new DesiredCapabilities(capabilitiesMap));
    }

}
