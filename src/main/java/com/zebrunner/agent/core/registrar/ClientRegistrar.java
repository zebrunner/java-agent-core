package com.zebrunner.agent.core.registrar;

public class ClientRegistrar {

    private static ZebrunnerApiClient client;

    public static ZebrunnerApiClient get() {
        if(client == null) {
            return DefaultZebrunnerApiClient.getInstance();
        }
        return client;
    }

    public static void registry(ZebrunnerApiClient newClient) {
        client = newClient;
    }

}
