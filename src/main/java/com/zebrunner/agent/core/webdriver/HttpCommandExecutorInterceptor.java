package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.remote.http.ClientConfig;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.concurrent.Callable;

@Slf4j
public class HttpCommandExecutorInterceptor {

    @RuntimeType
    public static Object constructor(@Morph Callable<?> callable,
            @AllArguments Object[] allArguments) throws Exception {
        if (ConfigurationHolder.shouldSubstituteRemoteWebDrivers()) {
            log.debug("Selenium Hub URL will be substituted by the value provided from Zebrunner.");
            int index = -1;
            for (int i = 0; i < allArguments.length; i++) {
                if (allArguments[i] instanceof ClientConfig) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                ClientConfig config = (ClientConfig) allArguments[index];
                URL seleniumHubUrl = RemoteWebDriverFactory.getSeleniumHubUrl();
                String userInfo = seleniumHubUrl.getUserInfo();
                if (userInfo != null && !userInfo.isEmpty()) {
                    String[] credentials = userInfo.split(":", 2);
                    String username = credentials[0];
                    String password = credentials.length > 1 ? credentials[1] : "";
                    config = config.authenticateAs(new UsernameAndPassword(username, password));
                }
                config = config.baseUri(seleniumHubUrl.toURI());
                allArguments[index] = config;
            } else {
                log.warn("Could not find ClientConfig parameter in HttpCommandExecutor class.");
            }
        }
        return callable.call();
    }
}
