package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.TestFinish;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinish;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStart;
import com.zebrunner.agent.core.registrar.descriptor.TestStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Zebrunner Agent API allowing to track test run events in Zebrunner
 */
public interface TestRunRegistrar {

    Logger LOGGER = LoggerFactory.getLogger(TestRunRegistrar.class);

    /**
     * Factory method allowing to obtain Zebrunner test run registrar
     *
     * @return Zebrunner registrar instance
     */
    static TestRunRegistrar getInstance() {
        if (ConfigurationHolder.isReportingEnabled()) {
            return ReportingRegistrar.getInstance();
        } else {
            LOGGER.warn("Reporting disabled: using no op test run registrar");
            return NoOpTestRunRegistrar.getInstance();
        }
    }

    /**
     * Registers test run start
     *
     * @param testRunStart test run start descriptor capturing state at the beginning of the run
     */
    void registerStart(TestRunStart testRunStart);

    /**
     * Registers test run finish
     *
     * @param testRunFinish test run finish descriptor capturing state at the end of the run
     */
    void registerFinish(TestRunFinish testRunFinish);

    void registerHeadlessTestStart(String id, TestStart testStart);

    /**
     * Registers test start
     *
     * @param id                  key that uniquely identifies specific test in scope of test run.
     *                            This value will be used later for test finish registration
     * @param testStart test start descriptor
     */
    void registerTestStart(String id, TestStart testStart);

    /**
     * Checks if there is a started test within current execution thread.
     *
     * @return true - if there is a started test, otherwise - false
     */
    boolean isTestStarted();

    /**
     * Checks whether a test with specific id has been started or not
     *
     * @param id key that uniquely identifies specific test in scope of test run.
     * @return true - if the test has been started, otherwise - false
     */
    boolean isTestStarted(String id);

    /**
     * Registers test finish
     *
     * @param id                   key that uniquely identifies specific test in scope of test run.
     *                             Appropriate test start with matching id should be registered prior to test finish registration,
     *                             otherwise test won't be properly registered
     * @param testFinish test result descriptor
     */
    void registerTestFinish(String id, TestFinish testFinish);

    void registerAfterTestStart();

    void registerAfterTestFinish();

    boolean isKnownIssueAttachedToTest(String failureStacktrace);

    void clearConfigurationLogs();

}
