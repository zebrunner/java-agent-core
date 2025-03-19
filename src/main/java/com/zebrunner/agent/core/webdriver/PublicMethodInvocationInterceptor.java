package com.zebrunner.agent.core.webdriver;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.Callable;

import com.zebrunner.agent.core.registrar.TestSessionRegistrar;

public class PublicMethodInvocationInterceptor {

    private static final TestSessionRegistrar REGISTRAR = TestSessionRegistrar.getInstance();

    @RuntimeType
    public static Object onPublicMethodInvocation(@This final RemoteWebDriver driver,
                                                  @SuperCall final Callable<Object> proxy) throws Exception {
        Object returnValue = proxy.call();

        String sessionId = driver.getSessionId().toString();
        if (sessionId.length() >= 64) {
            sessionId = sessionId.substring(32);
        }
        REGISTRAR.linkToCurrentTest(sessionId);

        return returnValue;
    }

}
