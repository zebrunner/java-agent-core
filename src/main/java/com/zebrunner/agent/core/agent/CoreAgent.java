package com.zebrunner.agent.core.agent;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class CoreAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        ServiceLoader.load(PremainInvocationListener.class)
                     .forEach(listener -> listener.onPremain(args, instrumentation));
    }

}
