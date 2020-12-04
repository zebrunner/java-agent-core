package com.zebrunner.agent.core.listener;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentListenerHolder {

    private static final List<RerunListener> RERUN_LISTENERS = new ArrayList<>();

    static {
        ServiceLoader<AgentListener> listeners = ServiceLoader.load(AgentListener.class);
        listeners.forEach(AgentListenerHolder::addListener);
    }

    private static void addListener(AgentListener listener) {
        if (listener instanceof RerunListener) {
            RERUN_LISTENERS.add((RerunListener) listener);
        }
    }

    public static List<RerunListener> getRerunListeners() {
        return RERUN_LISTENERS;
    }

}
