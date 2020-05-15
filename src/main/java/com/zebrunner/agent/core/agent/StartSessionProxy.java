package com.zebrunner.agent.core.agent;

import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.WebDriverListener;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.util.concurrent.Callable;

public class StartSessionProxy {

    @RuntimeType
    public static void onSessionStart(@SuperCall final Callable<Boolean> proxy, @Argument(0) Object capabilities) throws Exception {
        proxy.call();
        for(WebDriverListener listener : AgentListenerHolder.getWebDriverListeners()) {
            listener.onSessionStart(capabilities);
        }
    }
}
