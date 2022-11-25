package com.zebrunner.agent.core.registrar;

public class ClientRegistrar {

    private static ZebrunnerApiClient client;

    public static ZebrunnerApiClient getClient() {
        if (client == null) {
            return DefaultZebrunnerApiClient.getInstance();
        }
        return client;
    }

    public static void setClient(ZebrunnerApiClient newClient) {
        client = newClient;
    }
}
