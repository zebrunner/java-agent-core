package com.zebrunner.agent.core.registrar.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExchangeRunContextResponse {

    private String id;
    private boolean runExists;
    private boolean rerunOnlyFailedTests;
    private List<TestDTO> tests;
    private String fullExecutionPlanContext;

//    Before reverting
//    private String testRunUuid;
//    private String mode;
//
//    private boolean runAllowed;
//    private String reason;
//
//    private boolean runOnlySpecificTests;
//    private List<TestDTO> testsToRun;

}
