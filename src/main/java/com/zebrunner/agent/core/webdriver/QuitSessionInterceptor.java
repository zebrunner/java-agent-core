package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.registrar.DriverSessionRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.Callable;

public class QuitSessionInterceptor {

    private static final DriverSessionRegistrar REGISTRAR = DriverSessionRegistrar.getInstance();

    @RuntimeType
    public static void onSessionClose(@This final RemoteWebDriver driver,
                                      @SuperCall final Callable<Boolean> proxy) throws Exception {
        String sessionId = driver.getSessionId().toString();
        SessionCloseDescriptor context = new SessionCloseDescriptor(sessionId, driver.getCapabilities());

        proxy.call();

        REGISTRAR.registerClose(context);
    }

}
