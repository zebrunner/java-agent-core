package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.SessionCloseDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.domain.TestSessionDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
class SessionRegistrar implements TestSessionRegistrar {

    private static final SessionRegistrar INSTANCE = new SessionRegistrar();

    public static SessionRegistrar getInstance() {
        return INSTANCE;
    }

    private final ZebrunnerApiClient apiClient = ClientRegistrar.getClient();

    private final Map<String, TestSessionDTO> sessionIdToSession = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<String>> threadSessionIds = InheritableThreadLocal.withInitial(HashSet::new);

    @Override
    public void registerStart(SessionStartDescriptor startDescriptor) {
        log.debug("Registering test session start. {}", startDescriptor);
        TestSessionDTO testSession = TestSessionDTO.builder()
                                                   .sessionId(startDescriptor.getSessionId())
                                                   .initiatedAt(startDescriptor.getInitiatedAt())
                                                   .startedAt(startDescriptor.getStartedAt())
                                                   .status(startDescriptor.getStatus())
                                                   .capabilities(startDescriptor.getCapabilities())
                                                   .failureReason(startDescriptor.getFailureReason())
                                                   .desiredCapabilities(startDescriptor.getDesiredCapabilities())
                                                   .build();

        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testSession.getTestIds()::add);

        testSession = apiClient.startSession(RunContext.getZebrunnerRunId(), testSession);

        // if reporting is enabled and test session was actually registered
        if (testSession != null && testSession.getStatus() != TestSessionDTO.Status.FAILED) {
            sessionIdToSession.put(testSession.getSessionId(), testSession);
            threadSessionIds.get().add(testSession.getSessionId());
        }

        log.debug("Registration of test session start completed. {}", startDescriptor);
    }

    @Override
    public void registerClose(SessionCloseDescriptor closeDescriptor) {
        log.debug("Registering test session close. {}", closeDescriptor);
        TestSessionDTO testSession = sessionIdToSession.get(closeDescriptor.getSessionId());
        if (testSession != null) {
            testSession.setEndedAt(closeDescriptor.getEndedAt());

            apiClient.updateSession(RunContext.getZebrunnerRunId(), testSession);

            sessionIdToSession.remove(closeDescriptor.getSessionId());
            threadSessionIds.get().remove(closeDescriptor.getSessionId());
        }

        log.debug("Registration of test session close completed. {}", closeDescriptor);
    }

    @Override
    public void linkAllCurrentToTest(Long zebrunnerTestId) {
        threadSessionIds.get().forEach(sessionId -> link(sessionId, zebrunnerTestId));
    }

    @Override
    public void linkToCurrentTest(String sessionId) {
        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(currentTestId -> link(sessionId, currentTestId));
    }

    private void link(String sessionId, Long zebrunnerId) {
        TestSessionDTO testSession = sessionIdToSession.get(sessionId);
        if (testSession != null) {
            Set<Long> testIds = testSession.getTestIds();

            if (testIds.add(zebrunnerId)) {
                log.debug("Linking test '{}' to session '{}'", zebrunnerId, sessionId);
                apiClient.updateSession(RunContext.getZebrunnerRunId(), testSession);
            }
        }
    }

}
