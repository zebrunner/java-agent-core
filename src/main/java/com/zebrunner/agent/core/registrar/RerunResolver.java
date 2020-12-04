package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.listener.AgentListenerHolder;
import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.client.domain.TestDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class RerunResolver {

    private static CompletableFuture<List<TestDTO>> testToRerunFuture;
    private static String runId;

    synchronized static void resolve() {
        String runPattern = ConfigurationHolder.getRerunRunId(); // can be null when first run, if not null - rerun
        if (runPattern != null) {
            processRerun(runPattern);
        } else {
            testToRerunFuture = CompletableFuture.completedFuture(null);
        }
    }

    static String getRunId() {
        return runId;
    }

    static boolean isRerun() {
        if (testToRerunFuture == null) {
            resolve();
        }

        List<TestDTO> tests = getFutureResult(testToRerunFuture);
        return tests != null;
    }

    /**
     * Build test run plan according to rerun condition, initializes rerun context and loads available rerun listeners.
     *
     * @param runPattern pattern to be used to build rerun plan
     */
    private static void processRerun(String runPattern) {
        ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();
        testToRerunFuture = CompletableFuture.supplyAsync(() -> {
            RerunCondition rerunCondition = RerunConditionResolver.resolve(runPattern);
            runId = rerunCondition.getRunId();
            List<TestDTO> tests = apiClient.getTestsByCiRunId(rerunCondition);

            RerunContextHolder.setTests(tests);

            for (RerunListener listener : AgentListenerHolder.getRerunListeners()) {
                listener.onRerun(tests);
            }
            return tests;
        });
    }

    private static <F> F getFutureResult(CompletableFuture<F> completableFuture) {
        F result = null;
        try {
            result = completableFuture.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return result;
    }

}
