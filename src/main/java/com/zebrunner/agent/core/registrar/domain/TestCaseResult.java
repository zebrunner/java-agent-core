package com.zebrunner.agent.core.registrar.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TestCaseResult {

    private final TcmType tcmType;
    private final String testCaseId;
    private final String resultStatus;

}
