package com.zebrunner.agent.core.registrar;

public class ClientRegistrar {

    private static volatile ZebrunnerApiClient client;

    public static synchronized ZebrunnerApiClient getClient() {
        if (client == null) {
            return DefaultZebrunnerApiClient.getInstance();
        }
        return client;
    }

    public static synchronized void register(ZebrunnerApiClient newClient) {
        client = newClient;
    }

}
