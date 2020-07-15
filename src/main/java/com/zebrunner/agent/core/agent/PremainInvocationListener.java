package com.zebrunner.agent.core.agent;

import java.lang.instrument.Instrumentation;

public interface PremainInvocationListener {

    void onPremain(String args, Instrumentation instrumentation);

}
