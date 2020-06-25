package com.zebrunner.agent.core.agent;

import java.lang.instrument.Instrumentation;

public interface AgentDiscoverable {

    void onPremain(String args, Instrumentation instrumentation);

}
