package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;

public interface TestSessionRegistrar {

    static TestSessionRegistrar getInstance() {
        return ConfigurationHolder.isReportingEnabled()
                ? SessionRegistrar.getInstance()
                : NoOpTestSessionRegistrar.getInstance();
    }

    void registerStart(SessionStartDescriptor context);

    void registerClose(SessionCloseDescriptor context);

    void linkAllCurrentToTest(Long zebrunnerTestId);

    void linkToCurrentTest(String sessionId);

}
