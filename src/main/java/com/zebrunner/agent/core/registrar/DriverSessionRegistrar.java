package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;

public interface DriverSessionRegistrar {

    static DriverSessionRegistrar getInstance() {
        if (ConfigurationHolder.isReportingEnabled()) {
            return SessionRegistrar.getInstance();
        } else {
            return NoOpDriverSessionRegistrar.getInstance();
        }
    }

    void registerStart(SessionStartDescriptor context);

    void registerClose(SessionCloseDescriptor context);

    void linkAllCurrentToTest(Long zebrunnerId);

    void linkToCurrentTest(String sessionId);

}
