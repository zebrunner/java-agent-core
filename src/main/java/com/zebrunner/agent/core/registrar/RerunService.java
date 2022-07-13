package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.domain.ExchangeRunContextResponse;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public final class RerunService {

    private static volatile List<TestDTO> fullExecutionContextTests;

    synchronized public static List<TestDTO> retrieveFullExecutionContextTests() {
        if (fullExecutionContextTests != null) {
            return fullExecutionContextTests;
        }

        String fullExecutionPlanContext = RunContextHolder.getFullExecutionPlanContext();
        if (fullExecutionPlanContext == null || !RunContextHolder.isRerun()) {
            log.debug("Full execution plan context is missing or rerun is not yet started.");
            return Collections.emptyList();
        }

        ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();
        ExchangeRunContextResponse response = apiClient.exchangeRerunCondition(fullExecutionPlanContext);

        List<TestDTO> tests = response.getTests();
        fullExecutionContextTests = tests;
        return tests;
    }

}
