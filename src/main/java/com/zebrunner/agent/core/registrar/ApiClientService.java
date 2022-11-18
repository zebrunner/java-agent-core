package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.domain.AutenticationData;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.core.registrar.domain.TestRunDTO;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ApiClientService {

    @POST("/api/iam/v1/auth/refresh")
    Call<AutenticationData> getAuthData(@Body Map<String, String> refreshToken);
    @POST("/api/reporting/v1/test-runs")
    Call<TestRunDTO> getTestRunDTO(@Body TestRunDTO testRunDTO, @Query("projectKey") String projectKey);

    @POST("/api/reporting/v1/test-runs/{testRunId}/tests")
    Call<TestDTO> getTestDTO(@Path("testRunId") String testRunId, @Query("headless") boolean headless, @Body TestDTO test);

    @PUT("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}")
    Call<String> callTestFinish(@Path("testRunId") String testRunId, @Path("testId") String testId,
                                @Query("headless") boolean headless, @Body TestDTO testDTO);

    @PUT("/api/reporting/v1/test-runs/{testRunId}")
    Call<String> callTestRunFinish(@Body TestRunDTO testRun, @Path("testRunId") String testRunId);
}
