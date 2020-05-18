package com.zebrunner.agent.core.listener;

import com.zebrunner.agent.core.listener.domain.StartSessionContext;
import com.zebrunner.agent.core.listener.domain.WebDriverContext;

public interface WebDriverListener extends AgentListener {

    void onSessionStart(StartSessionContext context);

    void onSessionQuit(WebDriverContext context);

    void onSessionClose(WebDriverContext context);

}
