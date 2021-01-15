package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.ServerException;
import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.domain.ArtifactReferenceDTO;
import com.zebrunner.agent.core.registrar.domain.AuthDataDTO;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;
import com.zebrunner.agent.core.registrar.domain.ObjectMapperImpl;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.core.registrar.domain.TestRunDTO;
import com.zebrunner.agent.core.registrar.domain.TestSessionDTO;
import kong.unirest.Config;
import kong.unirest.ContentType;
import kong.unirest.GenericType;
import kong.unirest.GetRequest;
import kong.unirest.HeaderNames;
import kong.unirest.HttpResponse;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
class ZebrunnerApiClient {

    private static final String SERVER_ERROR_MSG_FORMAT = "%s\nResponse status code: %d.\nRaw response body: \n%s";

    private final static String REPORTING_ENDPOINT_FORMAT = "%s/api/reporting/v1/%s";
    private final static String IAM_ENDPOINT_FORMAT = "%s/api/iam/%s";

    private static ZebrunnerApiClient INSTANCE;

    private String apiHost;
    private String authToken;
    private ObjectMapper objectMapper;
    private volatile UnirestInstance client;

    private ZebrunnerApiClient() {
        if (ConfigurationHolder.isReportingEnabled()) {
            this.apiHost = ConfigurationHolder.getHost();
            this.objectMapper = new ObjectMapperImpl();
            this.client = initClient();

            AuthDataDTO authData = authenticateClient();
            authToken = authData.getAuthTokenType() + " " + authData.getAuthToken();

            Config config = client.config();
            config.addDefaultHeader(HeaderNames.AUTHORIZATION, authToken);
        }
    }

    static synchronized ZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ZebrunnerApiClient();
        }
        return INSTANCE;
    }

    private AuthDataDTO authenticateClient() {
        String refreshToken = ConfigurationHolder.getToken();
        HttpResponse<String> response = client.post(iam("v1/auth/refresh"))
                                              .body(Collections.singletonMap("refreshToken", refreshToken))
                                              .asString();

        if (!response.isSuccess()) {
            // null out the api client since it we cannot use it anymore
            client = null;

            throw new ServerException(formatErrorMessage("Not able to refresh access token.", response));
        }
        return objectMapper.readValue(response.getBody(), AuthDataDTO.class);
    }

    private String formatErrorMessage(String message, HttpResponse<String> response) {
        return String.format(SERVER_ERROR_MSG_FORMAT, message, response.getStatus(), response.getBody());
    }

    private String reporting(String endpointPath) {
        return String.format(REPORTING_ENDPOINT_FORMAT, apiHost, endpointPath);
    }

    private String iam(String endpointPath) {
        return String.format(IAM_ENDPOINT_FORMAT, apiHost, endpointPath);
    }

    private UnirestInstance initClient() {
        Config config = new Config();
        config.addDefaultHeader("Content-Type", "application/json");
        config.addDefaultHeader("Accept", "application/json");
        config.setObjectMapper(new ObjectMapperImpl());
        return new UnirestInstance(config);
    }

    TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        if (client != null) {
            HttpResponse<String> response = client.post(reporting("test-runs"))
                                                  .body(testRun)
                                                  .queryString("projectKey", ConfigurationHolder.getProjectKey())
                                                  .asString();

            if (!response.isSuccess()) {
                // null out the api client since it we cannot use it anymore
                client = null;

                throw new ServerException(formatErrorMessage("Could not register start of the test run.", response));
            }
            return objectMapper.readValue(response.getBody(), TestRunDTO.class);
        } else {
            return null;
        }
    }

    void registerTestRunFinish(TestRunDTO testRun) {
        if (client != null) {
            HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}"))
                                                  .body(testRun)
                                                  .routeParam("testRunId", testRun.getId().toString())
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not register finish of the test run.", response));
            }
        }
    }

    TestDTO registerTestStart(Long testRunId, TestDTO test, boolean headless) {
        if (client != null) {
            HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/tests"))
                                                  .body(test)
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .queryString("headless", headless)
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not register start of the test.", response));
            }
            return objectMapper.readValue(response.getBody(), TestDTO.class);
        } else {
            return null;
        }
    }

    TestDTO registerTestRerunStart(Long testRunId, Long testId, TestDTO test, boolean headless) {
        if (client != null) {
            HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/tests/{testId}"))
                                                  .body(test)
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .routeParam("testId", testId.toString())
                                                  .queryString("headless", headless)
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not register start of rerun of the test.", response));
            }
            return objectMapper.readValue(response.getBody(), TestDTO.class);
        } else {
            return null;
        }
    }

    TestDTO registerHeadlessTestUpdate(Long testRunId, TestDTO test) {
        if (client != null) {
            HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/tests/{testId}"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .routeParam("testId", test.getId().toString())
                                                  .queryString("headless", true)
                                                  .body(test)
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not register start of the test.", response));
            }
            return objectMapper.readValue(response.getBody(), TestDTO.class);
        } else {
            return null;
        }
    }

    void revertTestRegistration(Long testRunId, Long testId) {
        if (client != null) {
            HttpResponse<String> response = client.delete(reporting("test-runs/{testRunId}/tests/{testId}"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .routeParam("testId", testId.toString())
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not revert test registration.", response));
            }
        }
    }

    void registerTestFinish(Long testRunId, TestDTO test) {
        if (client != null) {
            HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/tests/{testId}"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .routeParam("testId", test.getId().toString())
                                                  .queryString("headless", false)
                                                  .body(test)
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not register finish of the test.", response));
            }
        }
    }

    void sendLogs(Collection<Log> logs, Long testRunId) {
        if (client != null) {
            HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/logs"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .body(logs)
                                                  .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage("Could not send a batch of test logs.", response));
            }
        }
    }

    void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt) {
        if (client != null) {
            HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/tests/{testId}/screenshots"))
                                                  .headerReplace("Content-Type", ContentType.IMAGE_PNG.getMimeType())
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .routeParam("testId", testId.toString())
                                                  .header("x-zbr-screenshot-captured-at", capturedAt.toString())
                                                  .body(screenshot)
                                                  .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage("Could not upload a screenshot.", response));
            }
        }
    }

    void uploadTestRunArtifact(InputStream artifact, String name, Long testRunId) {
        if (client != null) {
            HttpResponse<String> response = Unirest.post(reporting("test-runs/{testRunId}/artifacts"))
                                                   .header(HeaderNames.AUTHORIZATION, authToken)
                                                   .routeParam("testRunId", testRunId.toString())
                                                   .field("file", artifact, name)
                                                   .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage("Could not attach test run artifact with name " + name, response));
            }
        }
    }

    void uploadTestArtifact(InputStream artifact, String name, Long testRunId, Long testId) {
        if (client != null) {
            HttpResponse<String> response = Unirest.post(reporting("test-runs/{testRunId}/tests/{testId}/artifacts"))
                                                   .header(HeaderNames.AUTHORIZATION, authToken)
                                                   .routeParam("testRunId", testRunId.toString())
                                                   .routeParam("testId", testId.toString())
                                                   .field("file", artifact, name)
                                                   .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage("Could not attach test artifact with name " + name, response));
            }
        }
    }

    void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReferenceDTO artifactReference) {
        if (client != null) {
            List<ArtifactReferenceDTO> artifactReferences = Collections.singletonList(artifactReference);
            HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/artifact-references"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .body(Collections.singletonMap("items", artifactReferences))
                                                  .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage(
                        "Could not attach the following test run artifact reference: " + artifactReference,
                        response
                ));
            }
        }
    }

    void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReferenceDTO artifactReference) {
        if (client != null) {
            List<ArtifactReferenceDTO> artifactReferences = Collections.singletonList(artifactReference);
            HttpResponse<String> response = client
                    .put(reporting("test-runs/{testRunId}/tests/{testId}/artifact-references"))
                    .routeParam("testRunId", testRunId.toString())
                    .routeParam("testId", testId.toString())
                    .body(Collections.singletonMap("items", artifactReferences))
                    .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage(
                        "Could not attach the following test artifact reference: " + artifactReference,
                        response
                ));
            }
        }
    }

    void attachLabelsToTestRun(Long testRunId, Collection<LabelDTO> labels) {
        if (client != null) {
            HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/labels"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .body(Collections.singletonMap("items", labels))
                                                  .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage("Could not attach the following labels to test run: " + labels, response));
            }
        }
    }

    void attachLabelsToTest(Long testRunId, Long testId, Collection<LabelDTO> labels) {
        if (client != null) {
            HttpResponse<String> response = client
                    .put(reporting("test-runs/{testRunId}/tests/{testId}/labels"))
                    .routeParam("testRunId", testRunId.toString())
                    .routeParam("testId", testId.toString())
                    .body(Collections.singletonMap("items", labels))
                    .asString();

            if (!response.isSuccess()) {
                log.error(formatErrorMessage("Could not attach the following labels to test: " + labels, response));
            }
        }
    }

    List<TestDTO> getTestsByCiRunId(RerunCondition rerunCondition) {
        if (client != null) {
            GetRequest request = client.get(reporting("test-runs/{ciRunId}/tests"))
                                       .routeParam("ciRunId", rerunCondition.getRunId());

            setTestIds(request, rerunCondition.getTestIds());
            setStatuses(request, rerunCondition.getStatuses());

            HttpResponse<String> response = request.asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not get tests by ci run id.", response));
            }

            return objectMapper.readValue(response.getBody(), new GenericType<List<TestDTO>>() {
            });
        } else {
            return Collections.emptyList();
        }
    }

    private void setTestIds(GetRequest request, Set<Long> testIds) {
        if (!testIds.isEmpty()) {
            String tests = testIds.stream()
                                  .map(Object::toString)
                                  .collect(Collectors.joining(","));
            request.queryString("tests", tests);
        }
    }

    private void setStatuses(GetRequest request, Set<Status> testStatuses) {
        if (!testStatuses.isEmpty()) {
            String statuses = testStatuses.stream()
                                          .map(Enum::name)
                                          .collect(Collectors.joining(","));
            request.queryString("statuses", statuses);
        }
    }

    TestSessionDTO startSession(Long testRunId, TestSessionDTO testSession) {
        if (client != null) {
            HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/test-sessions"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .body(testSession)
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not register start of the test session.", response));
            }

            return objectMapper.readValue(response.getBody(), TestSessionDTO.class);
        } else {
            return null;
        }
    }

    void updateSession(Long testRunId, TestSessionDTO testSession) {
        if (client != null) {
            HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/test-sessions/{testSessionId}"))
                                                  .routeParam("testRunId", testRunId.toString())
                                                  .routeParam("testSessionId", testSession.getId().toString())
                                                  .body(testSession)
                                                  .asString();

            if (!response.isSuccess()) {
                throw new ServerException(formatErrorMessage("Could not update test session.", response));
            }
        }
    }

}
