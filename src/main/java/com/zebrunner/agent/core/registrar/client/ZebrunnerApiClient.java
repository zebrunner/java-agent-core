package com.zebrunner.agent.core.registrar.client;

import java.io.InputStream;
import java.util.Collection;

import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.client.request.CloseTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.request.FinishTestRequest;
import com.zebrunner.agent.core.registrar.client.request.FinishTestRunRequest;
import com.zebrunner.agent.core.registrar.client.request.StartHeadlessTestRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestRunRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.request.UpdateTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.response.ExchangeRunContextResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestRunResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestSessionResponse;
import com.zebrunner.agent.core.registrar.domain.ArtifactReference;
import com.zebrunner.agent.core.registrar.domain.Label;
import com.zebrunner.agent.core.registrar.domain.TestCaseResult;

public interface ZebrunnerApiClient {

    ExchangeRunContextResponse exchangeRunContext(String runContext);

    StartTestRunResponse registerTestRunStart(StartTestRunRequest startRequest);

    void patchTestRunBuild(Long testRunId, String build);

    void patchTestRunPlatform(Long testRunId, String platformName, String platformVersion);

    void registerTestRunFinish(Long testRunId, FinishTestRunRequest request);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    StartTestResponse registerTestStart(Long testRunId, StartTestRequest request);

    StartTestResponse registerHeadlessTestStart(Long testRunId, StartHeadlessTestRequest request);

    StartTestResponse registerTestRerunStart(Long testRunId, Long testId, StartTestRequest request);

    StartTestResponse registerHeadlessTestRerunStart(Long testRunId, Long testId, StartHeadlessTestRequest request);

    StartTestResponse registerHeadlessTestUpdate(Long testRunId, Long testId, StartTestRequest request);

    void revertTestRegistration(Long testRunId, Long testId);

    boolean isKnownIssueAttachedToTest(Long testRunId, Long testId, String failureStacktrace);

    void registerTestFinish(Long testRunId, Long testId, FinishTestRequest request);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void sendLogs(Collection<Log> logs, Long testRunId);

    void upsertTestCaseResults(Long testRunId, Long testId, Collection<TestCaseResult> testCaseResults);

    void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt);

    void uploadTestRunArtifact(InputStream artifact, String name, Long testRunId);

    void uploadTestArtifact(InputStream artifact, String name, Long testRunId, Long testId);

    void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReference artifactReference);

    void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReference artifactReference);

    void attachLabelsToTestRun(Long testRunId, Collection<Label> labels);

    void attachLabelsToTest(Long testRunId, Long testId, Collection<Label> labels);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    StartTestSessionResponse startSession(Long testRunId, StartTestSessionRequest request);

    void updateSession(Long testRunId, Long testSessionId, UpdateTestSessionRequest request);

    void closeSession(Long testRunId, Long testSessionId, CloseTestSessionRequest request);

}
