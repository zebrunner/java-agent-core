package com.zebrunner.agent.core.config;


public interface ConfigurationProvider {

    ReportingConfiguration getConfiguration();

    public static Long parseLong(String property) {
        try {
            return Long.valueOf(property);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean parseBoolean(String property) {
        if(property == null) {
            return null;
        }
        return Boolean.valueOf(property);
    }
}
