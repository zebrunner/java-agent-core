package com.zebrunner.agent.core.registrar.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TestCaseResult {

    private final TcmType tcmType;
    private final String testCaseId;
    private final String resultStatus;

    public static TestCaseResult ofTestRail(String testCaseId, String resultStatus) {
        return new TestCaseResult(TcmType.TEST_RAIL, testCaseId, resultStatus);
    }

    public static TestCaseResult ofXray(String testCaseId, String resultStatus) {
        return new TestCaseResult(TcmType.XRAY, testCaseId, resultStatus);
    }

    public static TestCaseResult ofZephyr(String testCaseId, String resultStatus) {
        return new TestCaseResult(TcmType.ZEPHYR, testCaseId, resultStatus);
    }

}
