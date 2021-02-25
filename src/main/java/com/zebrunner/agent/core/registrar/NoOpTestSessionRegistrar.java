package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NoOpTestSessionRegistrar implements TestSessionRegistrar {

    private static final NoOpTestSessionRegistrar INSTANCE = new NoOpTestSessionRegistrar();

    public static NoOpTestSessionRegistrar getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerStart(SessionStartDescriptor context) {
    }

    @Override
    public void registerClose(SessionCloseDescriptor context) {
    }

    @Override
    public void linkAllCurrentToTest(Long zebrunnerTestId) {
    }

    @Override
    public void linkToCurrentTest(String sessionId) {
    }

}
