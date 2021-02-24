package com.zebrunner.agent.core.registrar.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExchangeRerunConditionResponse {

    private String ciRunId;
    private boolean runExists;
    private boolean onlyFailedTests;
    private List<TestDTO> tests;

}
