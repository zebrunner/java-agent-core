package com.zebrunner.agent.core.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class AgentListenerHolder {

    private static final List<RerunListener> rerunListeners = new ArrayList<>();

    static {
        ServiceLoader<AgentListener> listeners = ServiceLoader.load(AgentListener.class);
        listeners.forEach(AgentListenerHolder::addListener);
    }

    private AgentListenerHolder() {
    }

    private static void addListener(AgentListener listener) {
        if (listener instanceof RerunListener) {
            rerunListeners.add((RerunListener) listener);
        }
    }

    public static List<RerunListener> getRerunListeners() {
        return rerunListeners;
    }

}
