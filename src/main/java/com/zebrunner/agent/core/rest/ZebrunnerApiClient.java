package com.zebrunner.agent.core.rest;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.rerun.RerunCondition;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import com.zebrunner.agent.core.rest.domain.TestRunDTO;
import kong.unirest.Config;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: 5/5/20 avoid public visibility
@Slf4j
public class ZebrunnerApiClient {

    private final static String REPORTING_API_CONTEXT_PATH = "api/reporting";
    private final static String REPORTING_API_VERSION = "v1";
    private final static String REPORTING_ENDPOINT_FORMAT = "%s/%s/%s/%s";

    private static ZebrunnerApiClient INSTANCE;

    private final String apiHost;
    private final UnirestInstance client;

    private ZebrunnerApiClient(String hostname, String accessToken) {
        this.apiHost = hostname;
        this.client = initClient(accessToken);
    }

    public static synchronized ZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            String host = ConfigurationHolder.getHost();
            String token = ConfigurationHolder.getToken();

            INSTANCE = new ZebrunnerApiClient(host, token);
        }
        return INSTANCE;
    }

    private String url(String endpointPath) {
        return String.format(REPORTING_ENDPOINT_FORMAT, apiHost, REPORTING_API_CONTEXT_PATH, REPORTING_API_VERSION, endpointPath);
    }

    private UnirestInstance initClient(String accessToken) {
        Config config = new Config();
        config.addDefaultHeader("Authorization", "Bearer " + accessToken);
        config.addDefaultHeader("Content-Type", "application/json");
        config.addDefaultHeader("Accept", "application/json");
        config.setObjectMapper(new ObjectMapperImpl());
        return new UnirestInstance(config);
    }

    public TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        HttpResponse<TestRunDTO> response = client.post(url("test-runs"))
                                                  .body(testRun)
                                                  .asObject(TestRunDTO.class);
        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return response.getBody();
    }

    public TestRunDTO registerTestRunFinish(TestRunDTO testRun) {
        HttpResponse<TestRunDTO> response = client.put(url("test-runs/{testRunId}"))
                                                  .body(testRun)
                                                  .routeParam("testRunId", String.valueOf(testRun.getId()))
                                                  .asObject(TestRunDTO.class);
        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return response.getBody();
    }

    public TestDTO registerTestStart(Long testRunId, TestDTO test) {
        HttpResponse<TestDTO> response = client.post(url("test-runs/{testRunId}/tests"))
                                               .body(test)
                                               .routeParam("testRunId", String.valueOf(testRunId))
                                               .asObject(TestDTO.class);
        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return response.getBody();
    }

    public TestDTO registerTestFinish(Long testRunId, TestDTO test) {
        HttpResponse<TestDTO> response = client.put(url("test-runs/{testRunId}/tests/{testId}"))
                                               .routeParam("testRunId", String.valueOf(testRunId))
                                               .routeParam("testId", String.valueOf(test.getId()))
                                               .body(test)
                                               .asObject(TestDTO.class);
        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return response.getBody();
    }

    @SuppressWarnings("rawtypes")
    public void sendLogs(Collection<Log> logs, String testRunId) {
        HttpResponse response = client.post(url("test-runs/{testRunId}/logs"))
                                      .routeParam("testRunId", testRunId)
                                      .body(logs)
                                      .asEmpty();
        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
    }

    @SuppressWarnings("rawtypes")
    public void sendScreenshot(byte[] screenshot, String testRunId, String testId, Long capturedAt) {
        HttpResponse response = client.post(url("test-runs/{testRunId}/tests/{testId}/screenshots"))
                                      .headerReplace("Content-Type", "image/png")
                                      .routeParam("testRunId", testRunId)
                                      .routeParam("testId", testId)
                                      .header("x-zbr-screenshot-captured-at", capturedAt.toString())
                                      .body(screenshot)
                                      .asEmpty();
        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
    }

    public List<TestDTO> getTestsByCiRunId(RerunCondition rerunCondition) {
        GetRequest request = client.get(url("test-runs/{ciRunId}/tests"))
                                   .routeParam("ciRunId", rerunCondition.getRunId());

        setTestIds(request, rerunCondition.getTestIds());
        setStatuses(request, rerunCondition.getStatuses());

        HttpResponse<TestDTO[]> response = request.asObject(TestDTO[].class);

        if (!response.isSuccess()) {
            throw new ServerException(response.getStatus(), response.getStatusText());
        }
        return Arrays.asList(response.getBody());
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
