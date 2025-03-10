package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.registrar.TestSessionRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.SessionClose;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class QuitSessionInterceptor {

    private static final TestSessionRegistrar REGISTRAR = TestSessionRegistrar.getInstance();

    // the map stores closed sessions. some projects may accidentally close a session more than once.
    // such behavior can lead to anomalies on Zebrunner side, thus it is better to control sessions close here
    private static final Map<String, Object> CLOSED_SESSIONS = new ConcurrentHashMap<>();
    private static final Object MAP_VALUE = new Object();

    @RuntimeType
    public static void onSessionClose(@This final RemoteWebDriver driver,
                                      @SuperCall final Callable<Boolean> proxy) throws Exception {
        String sessionId = driver.getSessionId().toString();
        if (sessionId.length() >= 64) {
            sessionId = sessionId.substring(32);
        }

        if (CLOSED_SESSIONS.put(sessionId, MAP_VALUE) == null) {
            SessionClose closeDescriptor = SessionClose.of(sessionId);

            REGISTRAR.registerClose(closeDescriptor);
        } else {
            log.warn("Session with id {} is closed more than once.", sessionId);
        }

        proxy.call();
    }

}
