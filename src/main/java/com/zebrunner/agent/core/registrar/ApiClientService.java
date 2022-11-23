package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.domain.*;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ApiClientService {

    @POST("/api/iam/v1/auth/refresh")
    Call<AutenticationData> getAuthData(@Body Map<String, String> refreshToken);
    @POST("/api/reporting/v1/test-runs")
    Call<TestRunDTO> getTestRunDTO(@Header("Authorization") String token, @Body TestRunDTO testRunDTO,
                                   @Query("projectKey") String projectKey);
    @POST("/api/reporting/v1/test-runs/{testRunId}/tests")
    Call<TestDTO> getTestDTO(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                             @Query("headless") boolean headless, @Body TestDTO test);

    @PUT("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}")
    Call<String> testFinishCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                @Path("testId") String testId, @Query("headless") boolean headless, @Body TestDTO testDTO);
    @PUT("/api/reporting/v1/test-runs/{testRunId}")
    Call<String> testRunFinishCall(@Header("Authorization") String token, @Body TestRunDTO testRun,
                                   @Path("testRunId") String testRunId);
    @POST("/api/reporting/v1/test-runs/{testRunId}/logs")
    Call<String> postLogs(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                          @Body Collection<Log> logs);
    @PUT("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}")
    Call<TestDTO> headlessTestUpdateCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                         @Path("testId") String testId, @Query("headless") boolean headless, @Body TestDTO test);
    @DELETE("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}")
    Call<String> revertTestRegistrationCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                            @Path("testId") String testId);

    @PATCH("/api/reporting/v1/test-runs/{testRunId}")
    Call<String> patchTestRunBuildCall(@Header("Authorization") String token, @Path("testRunId") String testRunId, @Body Map<String, String> build);

    @PUT("/api/reporting/v1/test-runs/{testRunId}/platform")
    Call<String> setTestRunPlatformCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                        @Body TestRunPlatform testRunPlatform);

    @POST("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}")
    Call<TestDTO> registerTestRerunStartCall(@Header("Authorization") String token, @Body TestDTO test, @Path("testRunId") String testRunId,
                                             @Path("testId") String testId, @Query("headless") boolean headless);

    @PUT("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}/labels")
    Call<String> attachLabelsToTestCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                        @Path("testId") String testId, @Body Map<String, Collection<LabelDTO>> labelMap);

    @PUT("/api/reporting/v1/test-runs/{testRunId}/labels")
    Call<String> attachLabelsToTestRunCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                           @Body Map<String, Collection<LabelDTO>> labelMap);

    @POST("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}/test-cases:upsert")
    Call<String> upsertTestCaseResultsCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                           @Path("testId") String testId, @Body Map<String, Collection<TestCaseResult>> testCaseMap);

    @POST("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}/screenshots")
    Call<String> uploadScreenshotCall(@Header("Authorization") String token, @Header("x-zbr-screenshot-captured-at") String capturedAt,
                                      @Path("testRunId") String testRunId, @Path("testId") String testId, @Body byte[] screenshot);
    @Multipart
    @POST("/api/reporting/v1/test-runs/{testRunId}/artifacts")
    Call<String> uploadTestRunArtifactCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                           @Part("file") MultipartBody.Part filePart);
    @Multipart
    @POST("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}/artifacts")
    Call<String> uploadTestArtifactCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                        @Path("testId") String testId, @Part("file") MultipartBody.Part filePart);
    @PUT("/api/reporting/v1/test-runs/{testRunId}/artifact-references")
    Call<String> attachArtifactReferenceToTestRunCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                                      @Body Map<String, List<ArtifactReferenceDTO>> requestBody);

    @PUT("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}/artifact-references")
    Call<String> attachArtifactReferenceToTestCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                                   @Path("testId") String testId, @Body Map<String, List<ArtifactReferenceDTO>> requestBody);
    @POST("/api/reporting/v1/run-context-exchanges")
    Call<ExchangeRunContextResponse> exchangeRerunConditionCall(@Header("Authorization") String token, @Body String rerunCondition);

    @POST("/api/reporting/v1/test-runs/{testRunId}/test-sessions")
    Call<TestSessionDTO> startSessionCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                          @Body TestSessionDTO testSession);
    @PUT("/api/reporting/v1/test-runs/{testRunId}/test-sessions/{testSessionId}")
    Call<String> updateSessionCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                   @Path("testSessionId") String testSessionId, @Body TestSessionDTO testSession);

    @POST("/api/reporting/v1/test-runs/{testRunId}/tests/{testId}/known-issue-confirmations")
    Call<KnownIssueConfirmation> isKnownIssueAttachedToTestCall(@Header("Authorization") String token, @Path("testRunId") String testRunId,
                                                                @Path("testId") String testId, @Body Map<String, String> failureRequest);
}
