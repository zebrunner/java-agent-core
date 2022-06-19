package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestCaseResult {

    private TcmType tcmType;
    private String testCaseId;
    private String resultStatus;

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
