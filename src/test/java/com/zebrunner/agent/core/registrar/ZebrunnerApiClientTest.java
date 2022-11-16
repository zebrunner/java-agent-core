package com.zebrunner.agent.core.registrar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ZebrunnerApiClientTest {

    @Test
    public void getInstance() {
        ZebrunnerApiClient client = ZebrunnerApiClient.getInstance();
        assertEquals(RetrofitZebrunnerApiClient.class, client.getClass());
    }

    @Test
    public void getDefaultInstanceFalse() {
        ZebrunnerApiClient client = ZebrunnerApiClient.getInstance();
        assertNotEquals(DefaultZebrunnerApiClient.class, client.getClass());
    }
}
