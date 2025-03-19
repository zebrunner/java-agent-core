package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ServiceLoader;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.TestAgentException;
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.client.response.ExchangeRunContextResponse;
import com.zebrunner.agent.core.registrar.domain.RunContextTestSummary;

@Slf4j
public final class RunContextService {

    private static Boolean isRerun;

    @Getter(AccessLevel.PACKAGE)
    private static String testRunUuid;

    private static String fullExecutionPlanContext;

    @SuppressWarnings("unused")
    public static boolean isRerun() {
        RunContextService.resolveRunContext();

        return Boolean.TRUE.equals(isRerun);
    }

    @SuppressWarnings("unused")
    // used by agent implementations for specific frameworks
    public static List<RunContextTestSummary> retrieveFullExecutionPlanTests() {
        RunContextService.resolveRunContext();

        if (fullExecutionPlanContext == null || Boolean.FALSE.equals(isRerun)) {
            log.debug("Full execution plan context is missing or rerun has not been initiated");
            return List.of();
        }

        return ApiClientRegistry.getClient()
                                .exchangeRunContext(fullExecutionPlanContext)
                                .getTestsToRun();
    }

    synchronized static void resolveRunContext() {
        if (isRerun == null) {
            if (!ConfigurationHolder.isReportingEnabled()) {
                isRerun = false;
            }

            String runContext = ConfigurationHolder.getRunContext();
            if (runContext != null) {
                RunContextService.processRunContext(runContext);
            } else {
                isRerun = Boolean.FALSE;
            }
        }
    }

    private static void processRunContext(String runContext) {
        ZebrunnerApiClient apiClient = ApiClientRegistry.getClient();
        ExchangeRunContextResponse response = apiClient.exchangeRunContext(runContext);

        if (response != null) {
            isRerun = response.getTestRunUuid() != null
                    ? RunContextService.processRunContextUsingNewFields(response)
                    : RunContextService.processRunContextUsingDeprecatedFields(response);
        } else {
            isRerun = false;
        }
    }

    private static boolean processRunContextUsingNewFields(ExchangeRunContextResponse response) {
        if (!response.isRunAllowed()) {
            throw new TestAgentException(response.getReason());
        }

        RunContextService.testRunUuid = response.getTestRunUuid();

        if (response.isRunOnlySpecificTests()) {
            RunContextService.fullExecutionPlanContext = response.getFullExecutionPlanContext();
            RunContextService.notifyRerunListenersAboutTestToRerun(response.getTestsToRun());

            return true;
        }

        return false;
    }

    private static boolean processRunContextUsingDeprecatedFields(ExchangeRunContextResponse response) {
        if (!response.isRunExists() && response.isRerunOnlyFailedTests()) {
            throw new TestAgentException("You cannot rerun failed tests because there is no test run with given ci run id in Zebrunner");
        }

        RunContextService.testRunUuid = response.getId();

        if (response.isRunExists()) {
            RunContextService.fullExecutionPlanContext = response.getFullExecutionPlanContext();
            RunContextService.notifyRerunListenersAboutTestToRerun(response.getTests());

            return true;
        }

        return false;
    }

    private static void notifyRerunListenersAboutTestToRerun(List<RunContextTestSummary> testsToRerun) {
        ServiceLoader.load(RerunListener.class)
                     .forEach(rerunListener -> rerunListener.onRerun(testsToRerun));
    }

}
