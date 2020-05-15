package com.zebrunner.agent.core.agent;

import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.WebDriverListener;
import net.bytebuddy.asm.Advice;

public class NewSessionProxy {

    @Advice.OnMethodExit
    public static void onSessionIdConstructor(@Advice.Origin String method,
                                              @Advice.AllArguments Object[] parameters,
                                              @Advice.FieldValue(value = "opaqueKey", readOnly = false) String opaqueKey) throws Exception {
        for(WebDriverListener listener : AgentListenerHolder.getWebDriverListeners()) {
            listener.onSessionCreate(opaqueKey);
        }
    }
}