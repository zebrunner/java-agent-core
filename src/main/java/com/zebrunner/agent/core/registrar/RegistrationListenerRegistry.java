package com.zebrunner.agent.core.registrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RegistrationListenerRegistry {

    private static final class InstanceHolder {

        private static final RegistrationListenerRegistry INSTANCE = new RegistrationListenerRegistry();

    }

    static RegistrationListenerRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final List<RegistrationListener> listeners = new ArrayList<>();

    private RegistrationListenerRegistry() {
        listeners.add(new TestRailCasesCollectingRegistrationListener());
        listeners.add(new XrayCasesCollectingRegistrationListener());
        listeners.add(new ZephyrCasesCollectingRegistrationListener());
        listeners.add(new TestCaseStatusSubmittingRegistrationListener());
    }

    public void forEach(Consumer<RegistrationListener> listenerConsumer) {
        listeners.forEach(listenerConsumer);
    }

}
