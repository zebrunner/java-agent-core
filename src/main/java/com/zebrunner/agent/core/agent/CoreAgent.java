package com.zebrunner.agent.core.agent;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class CoreAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        for (AgentDiscoverable agentDiscoverable : ServiceLoader.load(AgentDiscoverable.class)) {
            agentDiscoverable.onPremain(args, instrumentation);
        }
    }
}
