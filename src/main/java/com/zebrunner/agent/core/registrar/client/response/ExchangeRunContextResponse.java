package com.zebrunner.agent.core.registrar.client.response;

import lombok.Data;

import java.util.List;

import com.zebrunner.agent.core.registrar.domain.RunContextTestSummary;

@Data
public class ExchangeRunContextResponse {

    private String testRunUuid;

    private boolean runAllowed;
    private String reason;

    private boolean runOnlySpecificTests;
    private List<RunContextTestSummary> testsToRun;

    @Deprecated
    private String id;
    @Deprecated
    private boolean runExists;
    @Deprecated
    private boolean rerunOnlyFailedTests;
    @Deprecated
    private List<RunContextTestSummary> tests;
    @Deprecated
    private String fullExecutionPlanContext;

}
