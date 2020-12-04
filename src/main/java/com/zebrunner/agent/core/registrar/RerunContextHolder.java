package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.client.domain.TestDTO;

import java.util.List;

/**
 * Immutable (for client code outside registrar package) rerun context that is populated if this test run is actually a rerun.
 * Holds information on tests that are mean to be executed on rerun
 */
public final class RerunContextHolder {

    private static boolean rerun;
    private static List<TestDTO> tests;

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

    /**
     * Puts tests that are eligible for rerun to context. Automatically sets isRerun value to true.
     *
     * @param tests tests
     */
    static void setTests(List<TestDTO> tests) {
        RerunContextHolder.tests = tests;
        rerun = true;
    }

}
