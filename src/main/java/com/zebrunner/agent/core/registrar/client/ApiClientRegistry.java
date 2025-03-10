package com.zebrunner.agent.core.registrar.client;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;

public class ApiClientRegistry {

    @Getter
    private static volatile ZebrunnerApiClient client;

    static {
        String apiClientClass = System.getenv("REPORTING_API_CLIENT");
        if (apiClientClass == null) {
            apiClientClass = UnirestZebrunnerApiClient.class.getName();
        }

        try {
            client = (ZebrunnerApiClient) Class.forName(apiClientClass)
                                               .getDeclaredConstructor()
                                               .newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load Zebrunner API client class", e);
        } catch (InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException("Could not instantiate Zebrunner API client", e);
        }
    }

    public static synchronized void register(ZebrunnerApiClient newClient) {
        client = newClient;
    }

}
