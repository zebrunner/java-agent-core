package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.domain.TestDTO;

import java.util.List;

/**
 * Immutable (for client code outside registrar package) rerun context that is populated if this test run is actually a rerun.
 * Holds information on tests that are meant to be executed on rerun
 */
public final class RunContextHolder {

    private static boolean rerun;
    private static String testRunUuid;
    private static List<TestDTO> tests;
    private static String fullExecutionPlanContext;

    static String getTestRunUuid() {
        return testRunUuid;
    }

    static String getFullExecutionPlanContext() {
        return fullExecutionPlanContext;
    }

    public static List<TestDTO> getTests() {
        return tests;
    }

    public static boolean isRerun() {
        boolean result = false;
        // if already checked
        if (rerun) {
            result = true;
            // check if enabled
        } else if (ConfigurationHolder.isReportingEnabled()) {
            result = RerunResolver.isRerun();
        }
        return result;
    }

    static void setTestRunUuid(String testRunUuid) {
        RunContextHolder.testRunUuid = testRunUuid;
    }

    static void setFullExecutionPlanContext(String fullExecutionPlanContext) {
        RunContextHolder.fullExecutionPlanContext = fullExecutionPlanContext;
    }

    /**
     * Puts tests that are eligible for rerun to context. Automatically sets isRerun value to true.
     *
     * @param tests tests
     */
    static void setTests(List<TestDTO> tests) {
        RunContextHolder.tests = tests;
        rerun = true;
    }

}
