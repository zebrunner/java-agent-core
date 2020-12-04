package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.client.domain.TestSessionDTO;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class SessionRegistrar implements WebSessionRegistrar {

    private static final SessionRegistrar INSTANCE = new SessionRegistrar();

    public static SessionRegistrar getInstance() {
        return INSTANCE;
    }

    private final ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();

    private final Map<String, TestSessionDTO> sessionIdToSession = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<String>> threadSessionIds = InheritableThreadLocal.withInitial(HashSet::new);

    @Override
    public void registerStart(SessionStartDescriptor context) {
        String sessionId = context.getSessionId();
        TestSessionDTO testSession = TestSessionDTO.builder()
                                                   .sessionId(sessionId)
                                                   .startedAt(Instant.now())
                                                   .desiredCapabilities(context.getDesiredCapabilities())
                                                   .capabilities(context.getCapabilities())
                                                   .build();

        TestDescriptor currentTest = RunContext.getCurrentTest();
        if (currentTest != null) {
            testSession.getTestIds().add(currentTest.getZebrunnerId());
        }

        testSession = apiClient.startSession(RunContext.getRun().getZebrunnerId(), testSession);

        sessionIdToSession.put(testSession.getSessionId(), testSession);
        threadSessionIds.get().add(testSession.getSessionId());
    }

    @Override
    public void registerClose(SessionCloseDescriptor context) {
        TestSessionDTO testSession = sessionIdToSession.get(context.getSessionId());
        if (testSession == null) {
            throw new TestAgentException("Unable to end session. It is not started yet");
        }

        testSession.setEndedAt(Instant.now());

        apiClient.updateSession(RunContext.getRun().getZebrunnerId(), testSession);

        sessionIdToSession.remove(context.getSessionId());
        threadSessionIds.get().remove(context.getSessionId());
    }

    @Override
    public void linkAllCurrentToTest(Long zebrunnerId) {
        threadSessionIds.get().forEach(sessionId -> link(sessionId, zebrunnerId));
    }

    @Override
    public void linkToCurrentTest(String sessionId) {
        TestDescriptor currentTest = RunContext.getCurrentTest();
        if (currentTest != null) {
            link(sessionId, currentTest.getZebrunnerId());
        }
    }

    private void link(String sessionId, Long zebrunnerId) {
        TestSessionDTO testSession = sessionIdToSession.get(sessionId);
        if (testSession != null) {
            Set<Long> testIds = testSession.getTestIds();

            if (testIds.add(zebrunnerId)) {
                apiClient.updateSession(RunContext.getRun().getZebrunnerId(), testSession);
            }
        }
    }

}
