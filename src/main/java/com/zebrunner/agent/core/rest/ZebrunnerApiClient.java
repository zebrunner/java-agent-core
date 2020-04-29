package com.zebrunner.agent.core.rest;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.rerun.RerunCondition;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import com.zebrunner.agent.core.rest.domain.TestRunDTO;
import kong.unirest.Config;
import kong.unirest.GetRequest;
import kong.unirest.UnirestInstance;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZebrunnerApiClient {

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
        return apiHost + endpointPath;
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
        // TODO by nsidorevich on 2/25/20: POST /api/v2/test-runs
        return client.post(url("/api/v1/reporting/test-runs"))
                     .body(testRun)
                     .asObject(TestRunDTO.class)
                     .getBody();
    }

    public TestRunDTO registerTestRunFinish(TestRunDTO testRun) {
        // TODO by nsidorevich on 2/25/20: PUT /api/v2/test-runs/{testRunId}
        return client.put(url("/api/v1/reporting/test-runs/{testRunId}"))
                     .body(testRun)
                     .routeParam("testRunId", String.valueOf(testRun.getId()))
                     .asObject(TestRunDTO.class)
                     .getBody();
    }

    public TestDTO registerTestStart(Long testRunId, TestDTO test) {
        // TODO by nsidorevich on 2/25/20: POST /api/v2/test-runs/{testRunId}/tests
        return client.post(url("/api/v1/reporting/test-runs/{testRunId}/tests"))
                     .body(test)
                     .routeParam("testRunId", String.valueOf(testRunId))
                     .asObject(TestDTO.class)
                     .getBody();
    }

    public TestDTO registerTestFinish(Long testRunId, TestDTO test) {
        // TODO by nsidorevich on 2/25/20: PUT /api/v2/test-runs/{testRunId}/tests/{testId}
        return client.put(url("/api/v1/reporting/test-runs/{testRunId}/tests/{testId}"))
                     .routeParam("testRunId", String.valueOf(testRunId))
                     .routeParam("testId", String.valueOf(test.getId()))
                     .body(test)
                     .asObject(TestDTO.class)
                     .getBody();
    }

    public void sendLogs(Collection<Log> logs, String testRunId) {
        client.post(url("/reporting/test-runs/{testRunId}/logs"))
              .routeParam("testRunId", testRunId)
              .body(logs)
              .asEmpty();
    }

    public void sendScreenshot(byte[] screenshotBytes, String testRunId, String testId) {
        client.post(url("/reporting/test-runs/{testRunId}/tests/{testId}/screenshots"))
              .headerReplace("Content-Type", "image/png")
              .routeParam("testRunId", testRunId)
              .routeParam("testId", testId)
              .body(screenshotBytes)
              .asEmpty();
    }

    public List<TestDTO> getTestsByCiRunId(RerunCondition rerunCondition) {

        GetRequest request = client.get(url("/api/v1/reporting/test-runs/{ciRunId}/tests"))
                                   .routeParam("ciRunId", rerunCondition.getRunId());

        setTestIds(request, rerunCondition.getTestIds());
        setStatuses(request, rerunCondition.getStatuses());

        TestDTO[] tests =  request.asObject(TestDTO[].class).getBody();
        return Arrays.asList(tests);
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
