package com.zebrunner.agent.core.registrar.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GetTestsByCiRunIdResponse {

    private final boolean success;
    private final List<TestDTO> tests;

    public static GetTestsByCiRunIdResponse success(List<TestDTO> tests) {
        return new GetTestsByCiRunIdResponse(true, tests);
    }

    public static GetTestsByCiRunIdResponse runNotFound() {
        return new GetTestsByCiRunIdResponse(false, null);
    }

}
