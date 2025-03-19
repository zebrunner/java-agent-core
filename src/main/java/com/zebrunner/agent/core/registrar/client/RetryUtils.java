package com.zebrunner.agent.core.registrar.client;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
@UtilityClass
class RetryUtils {

    static <T> T tryInvoke(Supplier<T> action, int maxTries) {
        int tries = 0;
        while (true) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                ++tries;
                RetryUtils.handleException(e, maxTries, tries);
            }
        }
    }

    static void tryInvoke(Runnable action, int maxTries) {
        int tries = 0;
        while (true) {
            try {
                action.run();
                return;
            } catch (RuntimeException e) {
                ++tries;
                RetryUtils.handleException(e, maxTries, tries);
            }
        }
    }

    private static void handleException(RuntimeException e, int maxTries, int tries) {
        if (tries == maxTries || !RetryUtils.isVolatileRecoverableException(e)) {
            log.debug("All tries have been exhausted. Final exception is {}: {}", e.getClass(), e.getMessage());
            throw e;
        }

        log.debug("Trying to recover from exception {}: {}", e.getClass(), e.getMessage());
    }

    private static boolean isVolatileRecoverableException(Throwable e) {
        do {
            String message = e.getMessage();
            message = message != null
                    ? message.toLowerCase()
                    : "";
            if (message.contains("connection reset") || message.contains("unable to find valid certification path")) {
                return true;
            }
            e = e.getCause();
        } while (e != null && e != e.getCause());

        return false;
    }

}
