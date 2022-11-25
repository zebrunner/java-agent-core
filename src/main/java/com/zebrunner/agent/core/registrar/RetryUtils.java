package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class RetryUtils {

    public static <T> T tryInvoke(Supplier<T> action, Function<RuntimeException, Boolean> recoverableExceptionChecker, int maxTries) {
        int tries = 0;
        while (true) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                Boolean isRecoverableException = recoverableExceptionChecker.apply(e);
                if (!isRecoverableException || ++tries == maxTries) {
                    log.debug("All tries have been exhausted. Final exception is {}: {}", e.getClass(), e.getMessage());
                    throw e;
                }
                log.debug("Trying to recover from exception {}: {}", e.getClass(), e.getMessage());
            }
        }
    }

    public static void tryInvoke(Runnable action, Function<RuntimeException, Boolean> recoverableExceptionChecker, int maxTries) {
        int tries = 0;
        while (true) {
            try {
                action.run();
                return;
            } catch (RuntimeException e) {
                Boolean isRecoverableException = recoverableExceptionChecker.apply(e);
                if (!isRecoverableException || ++tries == maxTries) {
                    log.debug("All tries have been exhausted. Final exception is {}: {}", e.getClass(), e.getMessage());
                    throw e;
                }
                log.debug("Trying to recover from exception {}: {}", e.getClass(), e.getMessage());
            }
        }
    }

}
