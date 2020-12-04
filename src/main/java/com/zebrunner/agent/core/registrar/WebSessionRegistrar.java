package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;

public interface WebSessionRegistrar {

    static WebSessionRegistrar getInstance() {
        if (ConfigurationHolder.isReportingEnabled()) {
            return SessionRegistrar.getInstance();
        } else {
            return NoOpWebSessionRegistrar.getInstance();
        }
    }

    void registerStart(SessionStartDescriptor context);

    void registerClose(SessionCloseDescriptor context);

    void linkAllCurrentToTest(Long zebrunnerId);

    void linkToCurrentTest(String sessionId);

}
