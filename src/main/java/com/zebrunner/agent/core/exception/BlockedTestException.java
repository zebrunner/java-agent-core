package com.zebrunner.agent.core.exception;

/**
 * Tests that fail due to a {@code BlockedTestException} will have a "Blocked" status.
 * <p>
 * It is allowed to link an issue to blocked tests. Auto-linking, which links an issue if it determines
 * that the test failed due to a known issue, may consider the provided exception message to make this decision.
 * </p>
 */
public class BlockedTestException extends RuntimeException {

    public BlockedTestException(String message) {
        super(message);
    }

}
