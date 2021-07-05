package com.zebrunner.agent.core.webdriver;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import javax.annotation.Nullable;

// This class duplicates logic from org.openqa.selenium.remote.internal.OkHttpClient.Factory.builder method.
// This method sets Authorization header if a selenium hub url provided from Zebrunner contains credentials.
// Since we forcibly substitute the selenium hub url, we must substitute the Authenticator implementation as well.
public class BasicAuthenticator implements Authenticator {

    private final String authorizationHeaderValue;

    public BasicAuthenticator(String username, String password) {
        this.authorizationHeaderValue = Credentials.basic(username, password);
    }

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) {
        return response.request().header("Authorization") == null
                ? response.request().newBuilder().header("Authorization", authorizationHeaderValue).build()
                : null;
    }

}
