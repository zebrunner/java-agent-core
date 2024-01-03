package com.zebrunner.agent.core.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
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

    public static <T extends Enum<T>> T parseEnum(String property, Class<T> enumClass) {
        if (property == null) {
            return null;
        }

        T enumValue = Arrays.stream(enumClass.getEnumConstants())
                            .map(Enum::name)
                            .filter(enumName -> enumName.equalsIgnoreCase(property))
                            .findFirst()
                            .map(enumName -> Enum.valueOf(enumClass, enumName))
                            .orElse(null);

        if (enumValue == null) {
            log.warn("'{}' is not in the list of valid enum values: {}", property, enumClass.getEnumConstants());
        }

        return enumValue;
    }

}
