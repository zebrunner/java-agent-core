package com.zebrunner.agent.core.rest;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.rerun.RerunCondition;
import com.zebrunner.agent.core.rest.domain.AuthTokenDTO;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import com.zebrunner.agent.core.rest.domain.TestRunDTO;
import com.zebrunner.agent.core.rest.domain.TestSessionDTO;
import kong.unirest.Config;
import kong.unirest.ContentType;
import kong.unirest.GenericType;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: 5/5/20 avoid public visibility
@Slf4j
public class ZebrunnerApiClient {

    private final static String REPORTING_ENDPOINT_FORMAT = "%s/api/reporting/v1/%s";
    private final static String IAM_ENDPOINT_FORMAT = "%s/api/iam/%s";

    private static final String MULTIPART_MIME_TYPE = ContentType.MULTIPART_FORM_DATA.getMimeType();
    private static final String IMAGE_PNG_MIME_TYPE = ContentType.IMAGE_PNG.getMimeType();

    private static ZebrunnerApiClient INSTANCE;

    private final String apiHost;
    private final UnirestInstance client;
    private final ObjectMapper objectMapper;

    private ZebrunnerApiClient(String hostname, String accessToken) {
        this.apiHost = hostname;
        this.client = initClient();
        this.objectMapper = new ObjectMapperImpl();

        initAuthorization(accessToken);
    }

    public static synchronized ZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            String host = ConfigurationHolder.getHost();
            String token = ConfigurationHolder.getToken();

            INSTANCE = new ZebrunnerApiClient(host, token);
        }
        return INSTANCE;
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

    private void initAuthorization(String accessToken) {
        AuthTokenDTO authTokenDTO = refreshToken(accessToken);

        Config config = client.config();
        config.addDefaultHeader("Authorization", authTokenDTO.getAuthTokenType() + " " + authTokenDTO.getAuthToken());
    }

    public TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        HttpResponse<String> response = client.post(reporting("test-runs"))
                                              .body(testRun)
                                              .queryString("projectKey", ConfigurationHolder.getProjectKey())
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to register test run start. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestRunDTO.class);
    }

    public TestRunDTO registerTestRunFinish(TestRunDTO testRun) {
        HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}"))
                                              .body(testRun)
                                              .routeParam("testRunId", String.valueOf(testRun.getId()))
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to register test run finish. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestRunDTO.class);
    }

    public TestDTO registerTestStart(Long testRunId, TestDTO test, boolean headless) {
        HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/tests"))
                                              .body(test)
                                              .routeParam("testRunId", String.valueOf(testRunId))
                                              .queryString("headless", headless)
                                              .queryString("rerun", RerunContextHolder.isRerun())
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to register test start. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestDTO.class);
    }

    public TestDTO registerHeadlessTestUpdate(Long testRunId, TestDTO test) {
        HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/tests/{testId}"))
                                              .routeParam("testRunId", String.valueOf(testRunId))
                                              .routeParam("testId", String.valueOf(test.getId()))
                                              .queryString("headless", true)
                                              .body(test)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to register test start. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestDTO.class);
    }

    public TestDTO registerTestFinish(Long testRunId, TestDTO test) {
        HttpResponse<String> response = client.put(reporting("test-runs/{testRunId}/tests/{testId}"))
                                              .routeParam("testRunId", String.valueOf(testRunId))
                                              .routeParam("testId", String.valueOf(test.getId()))
                                              .queryString("headless", false)
                                              .body(test)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to register test finish. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestDTO.class);
    }

    public void sendLogs(Collection<Log> logs, String testRunId) {
        HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/logs"))
                                              .routeParam("testRunId", testRunId)
                                              .body(logs)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to send logs. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
    }

    public void uploadScreenshot(byte[] screenshot, String testRunId, String testId, Long capturedAt) {
        HttpResponse<String> response = client.post(reporting("test-runs/{testRunId}/tests/{testId}/screenshots"))
                                              .headerReplace("Content-Type", IMAGE_PNG_MIME_TYPE)
                                              .routeParam("testRunId", testRunId)
                                              .routeParam("testId", testId)
                                              .header("x-zbr-screenshot-captured-at", capturedAt.toString())
                                              .body(screenshot)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to send screenshot. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
    }

    public void uploadArtifact(InputStream artifact, String name, String testRunId, String testId) {
        HttpResponse<String> response = Unirest.post(reporting("test-runs/{testRunId}/tests/{testId}/artifacts"))
                                               .routeParam("testRunId", testRunId)
                                               .routeParam("testId", testId)
                                               .field("file", artifact, name)
                                               .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to send artifact with name {}. HTTP status: {}. Raw response: \n{}",
                    name, response.getStatus(), response.getBody()
            );
        }
    }

    public AuthTokenDTO refreshToken(String token) {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", token);
        HttpResponse<String> response = client.post(iam("v1/auth/refresh"))
                                              .body(request)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to refresh access token. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), AuthTokenDTO.class);
    }

    public List<TestDTO> getTestsByCiRunId(RerunCondition rerunCondition) {
        GetRequest request = client.get(reporting("test-runs/{ciRunId}/tests"))
                                   .routeParam("ciRunId", rerunCondition.getRunId());

        setTestIds(request, rerunCondition.getTestIds());
        setStatuses(request, rerunCondition.getStatuses());

        HttpResponse<String> response = request.asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to get tests by ci run id. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), new GenericType<List<TestDTO>>() {
        });
    }

    public TestSessionDTO startSession(TestSessionDTO testSession) {
        if (testSession.getStartedAt() == null) {
            testSession.setStartedAt(OffsetDateTime.now());
        }
        HttpResponse<String> response = client.post(reporting("test-sessions"))
                                              .body(testSession)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to start test session. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestSessionDTO.class);
    }

    public TestSessionDTO endSession(TestSessionDTO testSession) {
        if (testSession.getEndedAt() == null) {
            testSession.setEndedAt(OffsetDateTime.now());
        }
        return updateSession(testSession);
    }

    public TestSessionDTO updateSession(TestSessionDTO testSession) {
        HttpResponse<String> response = client.put(reporting("test-sessions/{testSessionId}"))
                                              .routeParam("testSessionId", testSession.getId().toString())
                                              .body(testSession)
                                              .asString();

        if (!response.isSuccess()) {
            log.error(
                    "Not able to update test session. HTTP status: {}. Raw response: \n{}",
                    response.getStatus(), response.getBody()
            );
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return objectMapper.readValue(response.getBody(), TestSessionDTO.class);
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

}
