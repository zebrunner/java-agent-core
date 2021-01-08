package com.zebrunner.agent.core.registrar.descriptor;

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
     * Indicates that test execution was aborted
     */
    ABORTED,

    /**
     * Indicates that test execution is still in progress
     */
    IN_PROGRESS

}
