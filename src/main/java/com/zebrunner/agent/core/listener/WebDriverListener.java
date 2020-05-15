package com.zebrunner.agent.core.listener;

public interface WebDriverListener extends AgentListener {

    void onSessionCreate(String opaqueKey);

    void onSessionStart(Object capabilities);

}
