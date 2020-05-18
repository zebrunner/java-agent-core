package com.zebrunner.agent.core.agent;

import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.domain.StartSessionContext;
import com.zebrunner.agent.core.listener.WebDriverListener;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.Callable;

public class StartSessionProxy {

    @RuntimeType
    public static void onSessionStart(@This final RemoteWebDriver driver, @SuperCall final Callable<Boolean> proxy, @Argument(0) Capabilities capabilities) throws Exception {
        proxy.call();

        StartSessionContext context = StartSessionContext.builder()
                                                         .desiredCapabilities(capabilities)
                                                         .actualCapabilities(driver.getCapabilities())
                                                         .sessionId(driver.getSessionId().toString())
                                                         .build();
        for (WebDriverListener listener : AgentListenerHolder.getWebDriverListeners()) {
            listener.onSessionStart(context);
        }
    }
}
