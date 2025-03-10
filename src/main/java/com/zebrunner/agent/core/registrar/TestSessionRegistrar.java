package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.SessionClose;
import com.zebrunner.agent.core.registrar.descriptor.SessionStart;

public interface TestSessionRegistrar {

    static TestSessionRegistrar getInstance() {
        return ConfigurationHolder.isReportingEnabled()
                ? SessionRegistrar.getInstance()
                : NoOpTestSessionRegistrar.getInstance();
    }

    void registerStart(SessionStart context);

    void registerClose(SessionClose context);

    void linkAllCurrentToTest(Long testId);

    void linkToCurrentTest(String sessionId);

}
