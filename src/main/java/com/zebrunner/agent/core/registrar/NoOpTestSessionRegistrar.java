package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.zebrunner.agent.core.registrar.domain.SessionClose;
import com.zebrunner.agent.core.registrar.domain.SessionStart;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NoOpTestSessionRegistrar implements TestSessionRegistrar {

    @Getter
    private static final NoOpTestSessionRegistrar instance = new NoOpTestSessionRegistrar();

    @Override
    public void registerStart(SessionStart context) {
    }

    @Override
    public void registerClose(SessionClose context) {
    }

    @Override
    public void linkAllCurrentToTest(Long testId) {
    }

    @Override
    public void linkToCurrentTest(String sessionId) {
    }

}
