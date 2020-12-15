package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.domain.TestSessionDTO;
import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
class SessionRegistrar implements DriverSessionRegistrar {

    private static final SessionRegistrar INSTANCE = new SessionRegistrar();

    public static SessionRegistrar getInstance() {
        return INSTANCE;
    }

    private final ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();

    private final Map<String, TestSessionDTO> sessionIdToSession = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<String>> threadSessionIds = InheritableThreadLocal.withInitial(HashSet::new);

    @Override
    public void registerStart(SessionStartDescriptor context) {
        log.debug("Registering test session start. {}", context);
        String sessionId = context.getSessionId();
        TestSessionDTO testSession = TestSessionDTO.builder()
                                                   .sessionId(sessionId)
                                                   .startedAt(Instant.now())
                                                   .capabilities(context.getCapabilities().asMap())
                                                   .desiredCapabilities(context.getDesiredCapabilities().asMap())
                                                   .build();

        TestDescriptor currentTest = RunContext.getCurrentTest();
        if (currentTest != null) {
            testSession.getTestIds().add(currentTest.getZebrunnerId());
        }

        testSession = apiClient.startSession(RunContext.getRun().getZebrunnerId(), testSession);

        sessionIdToSession.put(testSession.getSessionId(), testSession);
        threadSessionIds.get().add(testSession.getSessionId());

        log.debug("Registration of test session start completed. {}", context);
    }

    @Override
    public void registerClose(SessionCloseDescriptor context) {
        log.debug("Registering test session close. {}", context);
        TestSessionDTO testSession = sessionIdToSession.get(context.getSessionId());
        if (testSession == null) {
            throw new TestAgentException("Unable to end session. It is not started yet");
        }

        testSession.setEndedAt(Instant.now());

        apiClient.updateSession(RunContext.getRun().getZebrunnerId(), testSession);

        sessionIdToSession.remove(context.getSessionId());
        threadSessionIds.get().remove(context.getSessionId());

        log.debug("Registration of test session close completed. {}", context);
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
                log.debug("Linking test '{}' to session '{}'", zebrunnerId, sessionId);
                apiClient.updateSession(RunContext.getRun().getZebrunnerId(), testSession);
            }
        }
    }

}
