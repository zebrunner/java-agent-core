package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.registrar.domain.ExchangeRunContextResponse;
import com.zebrunner.agent.core.registrar.domain.TestDTO;

import java.util.List;

final class RerunResolver {

    private static String ciRunId;
    private static Boolean isRerun;

    synchronized static void resolve() {
        String runContext = ConfigurationHolder.getRunContext();
        if (runContext != null) {
            processRerun(runContext);
        } else {
            isRerun = Boolean.FALSE;
        }
    }

    static String getCiRunId() {
        return ciRunId;
    }

    static boolean isRerun() {
        if (isRerun == null) {
            resolve();
        }
        return Boolean.TRUE.equals(isRerun);
    }

    /**
     * Build test run plan according to rerun condition, initializes rerun context and loads available rerun listeners.
     *
     * @param rerunCondition pattern to be used to build rerun plan
     */
    private static void processRerun(String rerunCondition) {
        ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();
        ExchangeRunContextResponse response = apiClient.exchangeRerunCondition(rerunCondition);

        if (response != null) {
            if (!response.isRunExists() && response.isRerunOnlyFailedTests()) {
                throw new TestAgentException("You cannot rerun failed tests because there is no test run with given ci run id in Zebrunner");
            }

            ciRunId = response.getId();

            if (response.isRunExists()) {
                List<TestDTO> tests = response.getTests();
                RerunContextHolder.setTests(tests);

                for (RerunListener listener : AgentListenerHolder.getRerunListeners()) {
                    listener.onRerun(tests);
                }

                isRerun = true;
                return;
            }
        }

        isRerun = false;
    }

}
