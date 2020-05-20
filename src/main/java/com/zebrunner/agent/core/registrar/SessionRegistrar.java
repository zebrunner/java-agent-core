package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.listener.WebDriverListener;
import com.zebrunner.agent.core.listener.domain.StartSessionContext;
import com.zebrunner.agent.core.listener.domain.WebDriverContext;
import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import com.zebrunner.agent.core.rest.domain.TestSessionDTO;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistrar implements WebDriverListener {

    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    private static final Map<String, TestSessionDTO> testSessions = new ConcurrentHashMap<>();
    private static final ThreadLocal<Set<String>> threadSessionIds = new InheritableThreadLocal<>();
    private static final ThreadLocal<Set<String>> threadTestRefs = new InheritableThreadLocal<>();

    @Override
    public void onSessionStart(StartSessionContext context) {
        startSession(context);
    }

    @Override
    public void onSessionQuit(WebDriverContext context) {
        endSession(context);
    }

    @Override
    public void onSessionClose(WebDriverContext context) {
        endSession(context);
    }

    public static void addTestRef(String ref) {
        if (threadTestRefs.get() == null) {
            threadTestRefs.set(new HashSet<>());
        }
        threadTestRefs.get().add(ref);

        if (threadSessionIds.get() != null) {
            threadSessionIds.get().forEach(sessionId -> {
                TestSessionDTO testSession = testSessions.get(sessionId);
                testSession.getTestRefs().add(ref);

                API_CLIENT.updateSession(testSession);
            });
        }
    }

    public static void clearTestRef(String ref) {
        if (threadTestRefs.get() != null) {
            threadTestRefs.get().remove(ref);
        }
    }

    private void startSession(StartSessionContext context) {
        String sessionId = context.getSessionId();
        Set<String> testRefs = threadTestRefs.get();
        TestSessionDTO testSession = TestSessionDTO.builder()
                                                   .sessionId(sessionId)
                                                   .startedAt(OffsetDateTime.now())
                                                   .desiredCapabilities(context.getDesiredCapabilities())
                                                   .capabilities(context.getActualCapabilities())
                                                   .testRefs(testRefs)
                                                   .build();
        TestSessionDTO response = API_CLIENT.startSession(testSession);
        testSession.setId(response.getId());

        holdSession(sessionId, testSession);
    }

    private void endSession(WebDriverContext context) {
        String sessionId = context.getSessionId();
        Set<String> testRefs = threadTestRefs.get();
        TestSessionDTO testSession = testSessions.get(sessionId);
        if (testSession == null) {
            throw new TestAgentException("Unable to end session. It is not started yet");
        }

        testSession.setTestRefs(testRefs);
        testSession.setEndedAt(OffsetDateTime.now());
        API_CLIENT.endSession(testSession);

        releaseSession(sessionId);
    }

    private void holdSession(String sessionId, TestSessionDTO testSession) {
        testSessions.putIfAbsent(sessionId, testSession);
        if (threadSessionIds.get() == null) {
            threadSessionIds.set(new HashSet<>());
        }
        threadSessionIds.get().add(sessionId);
    }

    private void releaseSession(String sessionId) {
        testSessions.remove(sessionId);
        if (threadSessionIds.get() != null) {
            threadSessionIds.get().remove(sessionId);
            if (threadSessionIds.get().isEmpty()) {
                threadSessionIds.remove();
            }
        }
    }
}
