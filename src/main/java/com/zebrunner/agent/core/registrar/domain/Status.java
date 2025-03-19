package com.zebrunner.agent.core.registrar.domain;

/**
 * Resulting test status
 */
public enum Status {

    /**
     * Indicates that test execution was successful and it has passed
     */
    PASSED,

    /**
     * Indicates that test execution failed
     */
    FAILED,

    /**
     * Indicates that test execution was skipped
     */
    SKIPPED,

    /**
     * Indicates that test was failed because of {@link com.zebrunner.agent.core.exception.BlockedTestException}
     */
    BLOCKED,

    /**
     * Indicates that test execution was aborted
     */
    ABORTED,

    /**
     * Indicates that test execution is still in progress. Exists in this enum for edge cases and should not be explicitly used
     */
    IN_PROGRESS

}
