package com.zebrunner.agent.core.registrar.client;

import kong.unirest.Config;
import kong.unirest.ContentType;
import kong.unirest.HeaderNames;
import kong.unirest.HttpResponse;
import kong.unirest.MimeTypes;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.ServerException;
import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.client.request.CloseTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.request.FinishTestRequest;
import com.zebrunner.agent.core.registrar.client.request.FinishTestRunRequest;
import com.zebrunner.agent.core.registrar.client.request.StartHeadlessTestRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestRunRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.request.UpdateLaunchTcmConfigRequest;
import com.zebrunner.agent.core.registrar.client.request.UpdateTestSessionRequest;
import com.zebrunner.agent.core.registrar.client.response.ExchangeRunContextResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestRunResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestSessionResponse;
import com.zebrunner.agent.core.registrar.domain.ArtifactReference;
import com.zebrunner.agent.core.registrar.domain.AuthenticationData;
import com.zebrunner.agent.core.registrar.domain.KnownIssueConfirmation;
import com.zebrunner.agent.core.registrar.domain.Label;
import com.zebrunner.agent.core.registrar.domain.TestCaseResult;
import com.zebrunner.agent.core.registrar.domain.TestRunPlatform;

@Slf4j
public class UnirestZebrunnerApiClient implements ZebrunnerApiClient {

    private static UnirestZebrunnerApiClient INSTANCE;

    private String apiHost;
    private String authToken;
    private volatile UnirestInstance client;

    public UnirestZebrunnerApiClient() {
        if (ConfigurationHolder.isReportingEnabled()) {
            this.apiHost = ConfigurationHolder.getHost();
            this.client = this.initClient();

            this.authToken = this.authenticateClient();

            client.config()
                  .addDefaultHeader(HeaderNames.AUTHORIZATION, authToken);
        }
    }

    public static synchronized UnirestZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UnirestZebrunnerApiClient();
        }
        return INSTANCE;
    }

    private UnirestInstance initClient() {
        Config config = new Config().addDefaultHeader(HeaderNames.CONNECTION, "close")
                                    .addDefaultHeader(HeaderNames.CONTENT_TYPE, MimeTypes.JSON)
                                    .addDefaultHeader(HeaderNames.ACCEPT, MimeTypes.JSON)
                                    .setObjectMapper(new ObjectMapperImpl());
        return new UnirestInstance(config);
    }

    private String authenticateClient() {
        String refreshToken = ConfigurationHolder.getToken();
        HttpResponse<String> response = client.post(apiHost + "/api/iam/v1/auth/refresh")
                                              .body(Collections.singletonMap("refreshToken", refreshToken))
                                              .asObject(AuthenticationData.class)
                                              .map(authenticationData -> "Bearer " + authenticationData.getAuthToken());

        if (!response.isSuccess()) {
            // null out the api client since we cannot use it anymore
            client = null;
            this.throwServerException("Not able to obtain api token.", response);
        }
        return response.getBody();
    }

    private String reportingAPI(String endpointPath) {
        return String.format("%s/api/reporting%s", apiHost, endpointPath);
    }

    private String formatError(String message, HttpResponse<?> response) {
        return String.format(
                "%s\nResponse status code: %s.\nRaw response body: \n%s",
                message, response.getStatus(), response.mapError(String.class)
        );
    }

    private void throwServerException(String message, HttpResponse<?> response) {
        throw new ServerException(this.formatError(message, response));
    }

    private <T> T sendRequest(Function<UnirestInstance, HttpResponse<T>> requestExecutor) {
        if (client != null) {
            return RetryUtils.tryInvoke(() -> requestExecutor.apply(client).getBody(), 3);
        }
        return null;
    }

    private void sendVoidRequest(Consumer<UnirestInstance> requestExecutor) {
        if (client != null) {
            RetryUtils.tryInvoke(() -> requestExecutor.accept(client), 3);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ExchangeRunContextResponse exchangeRunContext(String runContext) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/run-context-exchanges"))
                      .body(runContext)
                      .asObject(ExchangeRunContextResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not exchange run context", response))
        );
    }

    @Override
    public StartTestRunResponse registerTestRunStart(StartTestRunRequest startRequest) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs"))
                      .queryString("projectKey", ConfigurationHolder.getProjectKey())
                      .body(startRequest)
                      .asObject(StartTestRunResponse.class)
                      .ifFailure(response -> {
                          // null out the api client since we cannot use it anymore
                          this.client = null;
                          this.throwServerException("Could not register start of the test run", response);
                      })
        );
    }

    @Override
    public void patchTestRunTcmConfig(Long testRunId, UpdateLaunchTcmConfigRequest request) {
        this.sendVoidRequest(client ->
                client.patch(reportingAPI("/v1/test-runs/{testRunId}/tcm-configs"))
                      .routeParam("testRunId", testRunId.toString())
                      .body(request)
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not patch TCM config of the test run", response))
        );
    }

    @Override
    public void patchTestRunBuild(Long testRunId, String build) {
        this.sendVoidRequest(client ->
                client.jsonPatch(reportingAPI("/v1/test-runs/{testRunId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .replace("/config/build", build)
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not patch build of the test run", response))
        );
    }

    @Override
    public void patchTestRunPlatform(Long testRunId, String platformName, String platformVersion) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/platform"))
                      .routeParam("testRunId", testRunId.toString())
                      .body(new TestRunPlatform(platformName, platformVersion))
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not set platform of the test run", response))
        );
    }

    @Override
    public void registerTestRunFinish(Long testRunId, FinishTestRunRequest request) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}"))
                      .body(request)
                      .routeParam("testRunId", testRunId.toString())
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not register finish of the test run", response))
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public StartTestResponse registerTestStart(Long testRunId, StartTestRequest request) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests"))
                      .routeParam("testRunId", testRunId.toString())
                      .queryString("headless", false)
                      .body(request)
                      .asObject(StartTestResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not register start of the test", response))
        );
    }

    @Override
    public StartTestResponse registerHeadlessTestStart(Long testRunId, StartHeadlessTestRequest request) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests"))
                      .routeParam("testRunId", testRunId.toString())
                      .queryString("headless", true)
                      .body(request)
                      .asObject(StartTestResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not register start of the headless test", response))
        );
    }

    @Override
    public StartTestResponse registerTestRerunStart(Long testRunId, Long testId, StartTestRequest request) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .queryString("headless", false)
                      .body(request)
                      .asObject(StartTestResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not register start of rerun of the test", response))
        );
    }

    @Override
    public StartTestResponse registerHeadlessTestRerunStart(Long testRunId, Long testId, StartHeadlessTestRequest request) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .queryString("headless", true)
                      .body(request)
                      .asObject(StartTestResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not register start of rerun of the headless test", response))
        );
    }

    @Override
    public StartTestResponse registerHeadlessTestUpdate(Long testRunId, Long testId, StartTestRequest request) {
        return this.sendRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .queryString("headless", true)
                      .body(request)
                      .asObject(StartTestResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not register start of the test", response))
        );
    }

    @Override
    public void revertTestRegistration(Long testRunId, Long testId) {
        this.sendVoidRequest(client ->
                client.delete(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not revert test registration", response))
        );
    }

    @Override
    public boolean isKnownIssueAttachedToTest(Long testRunId, Long testId, String failureStacktrace) {
        KnownIssueConfirmation confirmation = this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/known-issue-confirmations"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .body(Collections.singletonMap("failureReason", failureStacktrace))
                      .asObject(KnownIssueConfirmation.class)
                      .ifFailure(response -> this.throwServerException("Could not retrieve status of attached known issues", response))
        );
        return confirmation != null && confirmation.isKnownIssue();
    }

    @Override
    public void registerTestFinish(Long testRunId, Long testId, FinishTestRequest request) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .queryString("headless", false)
                      .body(request)
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not register finish of the test", response))
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void sendLogs(Collection<Log> logs, Long testRunId) {
        this.sendVoidRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/logs"))
                      .routeParam("testRunId", testRunId.toString())
                      .body(logs)
                      .asString()
                      .ifFailure(response -> log.error(this.formatError("Could not send a batch of test logs", response)))
        );
    }

    @Override
    public void upsertTestCaseResults(Long testRunId, Long testId, Collection<TestCaseResult> testCaseResults) {
        this.sendVoidRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/test-cases:upsert"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .body(Collections.singletonMap("testCases", testCaseResults))
                      .asString()
                      .ifFailure(response -> {
                          if (response.getStatus() == 404) {
                              log.warn("This functionality is not available for your Zebrunner distribution");
                          } else {
                              log.error(this.formatError("Could not send test case results", response));
                          }
                      })
        );
    }

    @Override
    public void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt) {
        this.sendVoidRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/screenshots"))
                      .headerReplace("Content-Type", ContentType.IMAGE_PNG.getMimeType())
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .header("x-zbr-screenshot-captured-at", capturedAt.toString())
                      .body(screenshot)
                      .asString()
                      .ifFailure(response -> log.error(this.formatError("Could not upload a screenshot", response)))
        );
    }

    @Override
    public void uploadTestRunArtifact(InputStream artifact, String name, Long testRunId) {
        this.sendVoidRequest(client ->
                Unirest.post(reportingAPI("/v1/test-runs/{testRunId}/artifacts"))
                       .header(HeaderNames.AUTHORIZATION, authToken)
                       .routeParam("testRunId", testRunId.toString())
                       .field("file", artifact, name)
                       .asString()
                       .ifFailure(response -> log.error(this.formatError("Could not attach test run artifact with name " + name, response)))
        );
    }

    @Override
    public void uploadTestArtifact(InputStream artifact, String name, Long testRunId, Long testId) {
        this.sendVoidRequest(client ->
                Unirest.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/artifacts"))
                       .header(HeaderNames.AUTHORIZATION, authToken)
                       .routeParam("testRunId", testRunId.toString())
                       .routeParam("testId", testId.toString())
                       .field("file", artifact, name)
                       .asString()
                       .ifFailure(response -> log.error(this.formatError("Could not attach test artifact with name " + name, response)))
        );
    }

    @Override
    public void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReference artifactReference) {
        Map<String, List<ArtifactReference>> requestBody = Collections.singletonMap(
                "items", Collections.singletonList(artifactReference)
        );
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/artifact-references"))
                      .routeParam("testRunId", testRunId.toString())
                      .body(requestBody)
                      .asString()
                      .ifFailure(response -> log.error(this.formatError(
                              "Could not attach the following test run artifact reference: " + artifactReference,
                              response
                      )))
        );
    }

    @Override
    public void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReference artifactReference) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/artifact-references"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .body(Collections.singletonMap("items", Collections.singletonList(artifactReference)))
                      .asString()
                      .ifFailure(response -> log.error(this.formatError(
                              "Could not attach the following test artifact reference: " + artifactReference,
                              response
                      )))
        );
    }

    @Override
    public void attachLabelsToTestRun(Long testRunId, Collection<Label> labels) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/labels"))
                      .routeParam("testRunId", testRunId.toString())
                      .body(Collections.singletonMap("items", labels))
                      .asString()
                      .ifFailure(response -> log.error(this.formatError("Could not attach the following labels to test run: " + labels, response)))
        );
    }

    @Override
    public void attachLabelsToTest(Long testRunId, Long testId, Collection<Label> labels) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/labels"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testId", testId.toString())
                      .body(Collections.singletonMap("items", labels))
                      .asString()
                      .ifFailure(response -> log.error(this.formatError("Could not attach the following labels to test: " + labels, response)))
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public StartTestSessionResponse startSession(Long testRunId, StartTestSessionRequest request) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/test-sessions"))
                      .routeParam("testRunId", testRunId.toString())
                      .body(request)
                      .asObject(StartTestSessionResponse.class)
                      .ifFailure(response -> this.throwServerException("Could not register start of the test session", response))
        );
    }

    @Override
    public void updateSession(Long testRunId, Long testSessionId, UpdateTestSessionRequest request) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/test-sessions/{testSessionId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testSessionId", testSessionId.toString())
                      .body(request)
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not update test session", response))
        );
    }

    @Override
    public void closeSession(Long testRunId, Long testSessionId, CloseTestSessionRequest request) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/test-sessions/{testSessionId}"))
                      .routeParam("testRunId", testRunId.toString())
                      .routeParam("testSessionId", testSessionId.toString())
                      .body(request)
                      .asString()
                      .ifFailure(response -> this.throwServerException("Could not close test session", response))
        );
    }

}
