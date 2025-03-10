package com.zebrunner.agent.core.registrar;

import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;

public class RegistrationListenerRegistry {

    @Getter
    private static final RegistrationListenerRegistry instance = new RegistrationListenerRegistry();

    private final List<RegistrationListener> listeners = List.of(
            TestRunTcmConfigurationRegistrationListener.getInstance(),
            TestRailCasesCollectingRegistrationListener.getInstance(),
            XrayCasesCollectingRegistrationListener.getInstance(),
            ZephyrCasesCollectingRegistrationListener.getInstance(),
            TestCasesCollectingRegistrationListener.getInstance(),
            TestCaseStatusSubmittingRegistrationListener.getInstance()
    );

    public void forEach(Consumer<RegistrationListener> listenerConsumer) {
        listeners.forEach(listenerConsumer);
    }

}
