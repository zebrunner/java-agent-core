package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.client.request.CloseTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.request.UpdateTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.response.StartTestSessionResponse;
import com.zebrunner.agent.core.registrar.descriptor.SessionClose;
import com.zebrunner.agent.core.registrar.descriptor.SessionStart;
import com.zebrunner.agent.core.registrar.domain.Test;
import com.zebrunner.agent.core.registrar.domain.TestSession;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SessionRegistrar implements TestSessionRegistrar {

    @Getter
    public static final SessionRegistrar instance = new SessionRegistrar();

    private final ZebrunnerApiClient apiClient = ApiClientRegistry.getClient();

    private final Map<String, TestSession> sessionIdToSession = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<String>> threadSessionIds = InheritableThreadLocal.withInitial(HashSet::new);

    @Override
    public void registerStart(SessionStart sessionStart) {
        log.debug("Registering test session start. {}", sessionStart);

        Long testRunId = ReportingContext.getNullableTestRunId();
        if (testRunId == null) {
            return;
        }

        var request = new StartTestSessionRequest().setSessionId(sessionStart.getSessionId())
                                                   .setInitiatedAt(sessionStart.getInitiatedAt())
                                                   .setStartedAt(sessionStart.getStartedAt())
                                                   .setStatus(sessionStart.getStatus())
                                                   .setFailureReason(sessionStart.getFailureReason())
                                                   .setDesiredCapabilities(sessionStart.getDesiredCapabilities())
                                                   .setCapabilities(sessionStart.getCapabilities());

        ReportingContext.getCurrentTest()
                        .map(Test::getId)
                        .ifPresent(request.getTestIds()::add);

        StartTestSessionResponse response = apiClient.startSession(testRunId, request);

        // if reporting is enabled and test session was actually registered
        if (response != null && response.getStatus() != TestSession.Status.FAILED) {
            TestSession testSession = TestSession.of(response, sessionStart);

            sessionIdToSession.put(testSession.getSessionId(), testSession);
            threadSessionIds.get().add(testSession.getSessionId());
        }

        log.debug("Registration of test session start completed. {}", sessionStart);
    }

    @Override
    public void registerClose(SessionClose sessionClose) {
        log.debug("Registering test session close. {}", sessionClose);

        Long testRunId = ReportingContext.getNullableTestRunId();
        TestSession testSession = sessionIdToSession.get(sessionClose.getSessionId());

        if (testSession != null && testRunId != null) {
            var request = new CloseTestSessionRequest().setEndedAt(sessionClose.getEndedAt());

            apiClient.closeSession(testRunId, testSession.getId(), request);

            sessionIdToSession.remove(sessionClose.getSessionId());
            threadSessionIds.get().remove(sessionClose.getSessionId());
        }

        log.debug("Registration of test session close completed. {}", sessionClose);
    }

    @Override
    public void linkAllCurrentToTest(Long testId) {
        threadSessionIds.get().forEach(sessionId -> this.link(sessionId, testId));
    }

    @Override
    public void linkToCurrentTest(String sessionId) {
        ReportingContext.getCurrentTest()
                        .map(Test::getId)
                        .ifPresent(currentTestId -> this.link(sessionId, currentTestId));
    }

    private void link(String sessionId, Long testId) {
        Long testRunId = ReportingContext.getNullableTestRunId();
        TestSession testSession = sessionIdToSession.get(sessionId);

        if (testSession != null && testSession.getTestIds().add(testId)) {
            log.debug("Linking test '{}' to session '{}'", testId, sessionId);

            var request = new UpdateTestSessionRequest().setTestIds(testSession.getTestIds());

            apiClient.updateSession(testRunId, testSession.getId(), request);
        }
    }

}
