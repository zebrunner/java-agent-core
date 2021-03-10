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

}
