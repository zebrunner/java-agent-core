package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.registrar.domain.GetTestsByCiRunIdResponse;
import com.zebrunner.agent.core.registrar.domain.TestDTO;

import java.util.List;

final class RerunResolver {

    private static String runId;
    private static Boolean isRerun;

    synchronized static void resolve() {
        String runPattern = ConfigurationHolder.getRerunRunId(); // can be null when first run, if not null - rerun
        if (runPattern != null) {
            processRerun(runPattern);
        } else {
            isRerun = Boolean.FALSE;
        }
    }

    static String getRunId() {
        return runId;
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
     * @param runPattern pattern to be used to build rerun plan
     */
    private static void processRerun(String runPattern) {
        ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();
        RerunCondition rerunCondition = RerunConditionResolver.resolve(runPattern);

        runId = rerunCondition.getRunId();
        GetTestsByCiRunIdResponse response = apiClient.getTestsByCiRunId(rerunCondition);

        if (response != null) {
            if (response.isSuccess()) {
                isRerun = true;

                List<TestDTO> tests = response.getTests();
                RerunContextHolder.setTests(tests);

                for (RerunListener listener : AgentListenerHolder.getRerunListeners()) {
                    listener.onRerun(tests);
                }
            } else if ("true".equalsIgnoreCase(System.getProperty("rerun_failures"))) {
                String formattedMessage = String.format(
                        "You cannot rerun failed tests because there is no test run with given ci run id (%s) in Zebrunner", runId
                );
                throw new TestAgentException(formattedMessage);
            }
        }

        isRerun = false;
    }

}
