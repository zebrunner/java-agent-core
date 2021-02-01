package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.registrar.DriverSessionRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.Callable;

public class StartSessionInterceptor {

    private static final DriverSessionRegistrar REGISTRAR = DriverSessionRegistrar.getInstance();

    @RuntimeType
    public static void onSessionStart(@This final RemoteWebDriver driver,
                                      @SuperCall final Callable<Boolean> proxy,
                                      @Argument(0) Capabilities capabilities) throws Exception {
        proxy.call();

        String sessionId = driver.getSessionId().toString();
        if (sessionId.length() >= 64 ) {
            // use case with GoGridRouter so we have to cut first 32 symbols!
            // have no idea what it actually means, but Vadim Delendik can provide more information
            sessionId = sessionId.substring(32);
        }
        SessionStartDescriptor context = new SessionStartDescriptor(sessionId, driver.getCapabilities(), capabilities);
        REGISTRAR.registerStart(context);
    }

}
