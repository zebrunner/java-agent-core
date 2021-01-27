package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
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
     * @param testRunStartDescriptor test run start descriptor capturing state at the beginning of the run
     */
    void registerStart(TestRunStartDescriptor testRunStartDescriptor);

    /**
     * Registers test run finish
     *
     * @param testRunFinishDescriptor test run finish descriptor capturing state at the end of the run
     */
    void registerFinish(TestRunFinishDescriptor testRunFinishDescriptor);

    void registerHeadlessTestStart(String id, TestStartDescriptor testStartDescriptor);

    /**
     * Registers test start
     *
     * @param id                  key that uniquely identifies specific test in scope of test run.
     *                            This value will be used later for test finish registration
     * @param testStartDescriptor test start descriptor
     */
    void registerTestStart(String id, TestStartDescriptor testStartDescriptor);

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
     * @param testFinishDescriptor test result descriptor
     */
    void registerTestFinish(String id, TestFinishDescriptor testFinishDescriptor);

}
