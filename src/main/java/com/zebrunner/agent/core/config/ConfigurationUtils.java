package com.zebrunner.agent.core.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationUtils {

    public static Long parseLong(String property) {
        try {
            return Long.valueOf(property);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean parseBoolean(String property) {
        if (property == null || !(property.equalsIgnoreCase("true") || property.equalsIgnoreCase("false"))) {
            return null;
        }
        return Boolean.valueOf(property);
    }

}
