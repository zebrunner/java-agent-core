package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.ServerException;
import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.domain.*;
import kong.unirest.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class DefaultZebrunnerApiClient implements ZebrunnerApiClient{
    private static DefaultZebrunnerApiClient INSTANCE;

    private String apiHost;
    private String authToken;
    private volatile UnirestInstance client;

    private DefaultZebrunnerApiClient() {
        if (ConfigurationHolder.isReportingEnabled()) {
            this.apiHost = ConfigurationHolder.getHost();
            this.client = this.initClient();

            this.authToken = this.authenticateClient();

            Config config = client.config();
            config.addDefaultHeader(HeaderNames.AUTHORIZATION, authToken);
        }
    }

    static synchronized DefaultZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultZebrunnerApiClient();
        }
        return INSTANCE;
    }

    private UnirestInstance initClient() {
        Config config = new Config();
        config.addDefaultHeader(HeaderNames.CONNECTION, "close");
        config.addDefaultHeader(HeaderNames.CONTENT_TYPE, MimeTypes.JSON);
        config.addDefaultHeader(HeaderNames.ACCEPT, MimeTypes.JSON);
        config.setObjectMapper(new ObjectMapperImpl());
        return new UnirestInstance(config);
    }

    private String authenticateClient() {
        String refreshToken = ConfigurationHolder.getToken();
        HttpResponse<String> response = client.post(apiHost + "/api/iam/v1/auth/refresh")
                .body(Collections.singletonMap("refreshToken", refreshToken))
                .asObject(AutenticationData.class)
                .map(authData -> authData.getAuthTokenType() + " " + authData.getAuthToken());

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
            return RetryUtils.tryInvoke(
                    () -> requestExecutor.apply(client).getBody(),
                    this::isVolatileRecoverableException,
                    3
            );
        }
        return null;
    }

    private void sendVoidRequest(Consumer<UnirestInstance> requestExecutor) {
        if (client != null) {
            RetryUtils.tryInvoke(
                    () -> requestExecutor.accept(client),
                    this::isVolatileRecoverableException,
                    3
            );
        }
    }

    private boolean isVolatileRecoverableException(Throwable e) {
        do {
            String message = e.getMessage();
            message = message != null
                    ? message.toLowerCase()
                    : "";
            if (message.contains("connection reset") || message.contains("unable to find valid certification path")) {
                return true;
            }
            e = e.getCause();
        } while (e != null && e != e.getCause());

        return false;
    }

    public TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs"))
                        .body(testRun)
                        .queryString("projectKey", ConfigurationHolder.getProjectKey())
                        .asObject(TestRunDTO.class)
                        .ifFailure(response -> {
                            // null out the api client since we cannot use it anymore
                            this.client = null;
                            this.throwServerException("Could not register start of the test run.", response);
                        })
        );
    }

    public void patchTestRunBuild(Long testRunId, String build) {
        this.sendVoidRequest(client ->
                client.jsonPatch(reportingAPI("/v1/test-runs/{testRunId}"))
                        .routeParam("testRunId", testRunId.toString())
                        .replace("/config/build", build)
                        .asString()
                        .ifFailure(response -> this.throwServerException("Could not patch build of the test run.", response))
        );
    }

    public void setTestRunPlatform(Long testRunId, String platformName, String platformVersion) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/platform"))
                        .routeParam("testRunId", testRunId.toString())
                        .body(new TestRunPlatform(platformName, platformVersion))
                        .asString()
                        .ifFailure(response -> this.throwServerException("Could not set platform of the test run.", response))
        );
    }

    public void registerTestRunFinish(TestRunDTO testRun) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}"))
                        .body(testRun)
                        .routeParam("testRunId", testRun.getId().toString())
                        .asString()
                        .ifFailure(response -> this.throwServerException("Could not register finish of the test run.", response))
        );
    }

    public TestDTO registerTestStart(Long testRunId, TestDTO test, boolean headless) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests"))
                        .body(test)
                        .routeParam("testRunId", testRunId.toString())
                        .queryString("headless", headless)
                        .asObject(TestDTO.class)
                        .ifFailure(response -> this.throwServerException("Could not register start of the test.", response))
        );
    }

    public TestDTO registerTestRerunStart(Long testRunId, Long testId, TestDTO test, boolean headless) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                        .body(test)
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", testId.toString())
                        .queryString("headless", headless)
                        .asObject(TestDTO.class)
                        .ifFailure(response -> this.throwServerException("Could not register start of rerun of the test.", response))
        );
    }

    public TestDTO registerHeadlessTestUpdate(Long testRunId, TestDTO test) {
        return this.sendRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", test.getId().toString())
                        .queryString("headless", true)
                        .body(test)
                        .asObject(TestDTO.class)
                        .ifFailure(response -> this.throwServerException("Could not register start of the test.", response))
        );
    }

    public void revertTestRegistration(Long testRunId, Long testId) {
        this.sendVoidRequest(client ->
                client.delete(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", testId.toString())
                        .asString()
                        .ifFailure(response -> this.throwServerException("Could not revert test registration.", response))
        );
    }

    public void registerTestFinish(Long testRunId, TestDTO test) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}"))
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", test.getId().toString())
                        .queryString("headless", false)
                        .body(test)
                        .asString()
                        .ifFailure(response -> this.throwServerException("Could not register finish of the test.", response))
        );
    }

    public void sendLogs(Collection<Log> logs, Long testRunId) {
        this.sendVoidRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/logs"))
                        .routeParam("testRunId", testRunId.toString())
                        .body(logs)
                        .asString()
                        .ifFailure(response -> log.error(this.formatError("Could not send a batch of test logs.", response)))
        );
    }

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
                                log.error(this.formatError("Could not send test case results.", response));
                            }
                        })
        );
    }

    public void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt) {
        this.sendVoidRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/screenshots"))
                        .headerReplace("Content-Type", ContentType.IMAGE_PNG.getMimeType())
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", testId.toString())
                        .header("x-zbr-screenshot-captured-at", capturedAt.toString())
                        .body(screenshot)
                        .asString()
                        .ifFailure(response -> log.error(this.formatError("Could not upload a screenshot.", response)))
        );
    }

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

    public void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReferenceDTO artifactReference) {
        Map<String, List<ArtifactReferenceDTO>> requestBody = Collections.singletonMap(
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

    public void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReferenceDTO artifactReference) {
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

    public void attachLabelsToTestRun(Long testRunId, Collection<LabelDTO> labels) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/labels"))
                        .routeParam("testRunId", testRunId.toString())
                        .body(Collections.singletonMap("items", labels))
                        .asString()
                        .ifFailure(response -> log.error(this.formatError("Could not attach the following labels to test run: " + labels, response)))
        );
    }

    public void attachLabelsToTest(Long testRunId, Long testId, Collection<LabelDTO> labels) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/labels"))
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", testId.toString())
                        .body(Collections.singletonMap("items", labels))
                        .asString()
                        .ifFailure(response -> log.error(this.formatError("Could not attach the following labels to test: " + labels, response)))
        );
    }

    public ExchangeRunContextResponse exchangeRerunCondition(String rerunCondition) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/run-context-exchanges"))
                        .body(rerunCondition)
                        .asObject(ExchangeRunContextResponse.class)
                        .ifFailure(response -> this.throwServerException("Could not get tests by ci run id.", response))
        );
    }

    public TestSessionDTO startSession(Long testRunId, TestSessionDTO testSession) {
        return this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/test-sessions"))
                        .routeParam("testRunId", testRunId.toString())
                        .body(testSession)
                        .asObject(TestSessionDTO.class)
                        .ifFailure(response -> this.throwServerException("Could not register start of the test session.", response))
        );
    }

    public void updateSession(Long testRunId, TestSessionDTO testSession) {
        this.sendVoidRequest(client ->
                client.put(reportingAPI("/v1/test-runs/{testRunId}/test-sessions/{testSessionId}"))
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testSessionId", testSession.getId().toString())
                        .body(testSession)
                        .asString()
                        .ifFailure(response -> this.throwServerException("Could not update test session.", response))
        );
    }

    public boolean isKnownIssueAttachedToTest(Long testRunId, Long testId, String failureStacktrace) {
        KnownIssueConfirmation confirmation = this.sendRequest(client ->
                client.post(reportingAPI("/v1/test-runs/{testRunId}/tests/{testId}/known-issue-confirmations"))
                        .routeParam("testRunId", testRunId.toString())
                        .routeParam("testId", testId.toString())
                        .body(Collections.singletonMap("failureReason", failureStacktrace))
                        .asObject(KnownIssueConfirmation.class)
                        .ifFailure(response -> this.throwServerException("Could not retrieve status of attached known issues.", response))
        );
        return confirmation != null && confirmation.isKnownIssue();
    }
}
