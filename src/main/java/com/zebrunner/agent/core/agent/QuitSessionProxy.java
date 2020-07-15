package com.zebrunner.agent.core.agent;

import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.domain.WebDriverContext;
import com.zebrunner.agent.core.listener.WebDriverListener;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.Callable;

public class QuitSessionProxy {

    @RuntimeType
    public static void onSessionQuit(@This final RemoteWebDriver driver, @SuperCall final Callable<Boolean> proxy) throws Exception {
        WebDriverContext context = WebDriverContext.builder()
                                                   .actualCapabilities(driver.getCapabilities())
                                                   .sessionId(driver.getSessionId().toString())
                                                   .build();

        proxy.call();

        for (WebDriverListener listener : AgentListenerHolder.getWebDriverListeners()) {
            listener.onSessionQuit(context);
        }
    }
}
