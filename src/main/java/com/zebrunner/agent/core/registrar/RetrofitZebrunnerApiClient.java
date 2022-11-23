package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.ServerException;
import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.domain.*;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
public class RetrofitZebrunnerApiClient implements ZebrunnerApiClient {

    private static RetrofitZebrunnerApiClient INSTANCE;
    private volatile ApiClientService client;
    private volatile String token;

    private RetrofitZebrunnerApiClient() {
        if (ConfigurationHolder.isReportingEnabled()) {
            client = initClient();
        }
    }

    static synchronized RetrofitZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RetrofitZebrunnerApiClient();
        }
        return INSTANCE;
    }

    private ApiClientService initClient() {
        return RetrofitServiceGenerator.createService(ApiClientService.class);
    }

    private String formatError(String message, Response<?> response) {
        return String.format(
                "%s\nResponse status code: %s.\nRaw response body: \n%s",
                message, response.code(), response.errorBody()
        );
    }

    private <T> T sendRequest(Function<ApiClientService, Response<T>> requestExecutor) {
        if (client != null) {
            return RetryUtils.tryInvoke(
                    () -> requestExecutor.apply(client)
                                         .body(),
                    this::isVolatileRecoverableException,
                    3
            );
        }
        return null;
    }

    private void sendVoidRequest(Consumer<ApiClientService> requestExecutor) {
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

    private void throwServerException(String message, Response<?> response) {
        throw new ServerException(this.formatError(message, response));
    }

    @Override
    public TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        String token = this.obtainToken();
        return this.sendRequest(client -> {
            Call<TestRunDTO> call = client.getTestRunDTO(token, testRun, ConfigurationHolder.getProjectKey());
            try {
                Response<TestRunDTO> response = call.execute();
                if (!response.isSuccessful()) {
                    // null out the api client since we cannot use it anymore
                    client = null;
                    this.throwServerException("Could not register start of the test run.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //TODO 2022-11-22
    @Override
    public void patchTestRunBuild(Long testRunId, String build) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.patchTestRunBuildCall(token, testRunId.toString(), Collections.singletonMap("/config/build", build));
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    this.throwServerException("Could not patch build of the test run.", response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //TODO 2022-11-22
    @Override
    public void setTestRunPlatform(Long testRunId, String platformName, String platformVersion) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.setTestRunPlatformCall(token, testRunId.toString(), new TestRunPlatform(platformName, platformVersion));
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    this.throwServerException("Could not set platform of the test run.", response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void registerTestRunFinish(TestRunDTO testRun) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.testRunFinishCall(token, testRun, testRun.getId().toString());
            try {
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not register finish of the test run.", response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public TestDTO registerTestStart(Long testRunId, TestDTO test, boolean headless) {
        String token = obtainToken();
        return this.sendRequest(client -> {
            Call<TestDTO> call = client.getTestDTO(token, testRunId.toString(), headless, test);
            try {
                Response<TestDTO> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not register start of the test.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public TestDTO registerTestRerunStart(Long testRunId, Long testId, TestDTO test, boolean headless) {
        return this.sendRequest(client -> {
            Call<TestDTO> call = client.registerTestRerunStartCall(token, test, testRunId.toString(), testId.toString(), headless);
            try {
                Response<TestDTO> response = call.execute();
                if(!response.isSuccessful()) {
                    this.throwServerException("Could not register start of rerun of the test.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public TestDTO registerHeadlessTestUpdate(Long testRunId, TestDTO test) {
        String token = obtainToken();
        return this.sendRequest(client -> {
            Call<TestDTO> call = client.headlessTestUpdateCall(token, testRunId.toString(), test.getId().toString(), true, test);
            try {
                Response<TestDTO> response = call.execute();
                if(!response.isSuccessful()) {
                    this.throwServerException("Could not register start of the test.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void revertTestRegistration(Long testRunId, Long testId) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.revertTestRegistrationCall(token, testRunId.toString(), testId.toString());
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    this.throwServerException("Could not revert test registration.", response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void registerTestFinish(Long testRunId, TestDTO test) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.testFinishCall(token, testRunId.toString(), test.getId().toString(), false, test);
            try {
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not register finish of the test.", response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void sendLogs(Collection<Log> logs, Long testRunId) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.postLogs(token, testRunId.toString(), logs);
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    log.error(this.formatError("Could not send a batch of test logs.", response));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void upsertTestCaseResults(Long testRunId, Long testId, Collection<TestCaseResult> testCaseResults) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.upsertTestCaseResultsCall(token, testRunId.toString(), testId.toString(),
                    Collections.singletonMap("testCases", testCaseResults));
            try {
                Response<String> response = call.execute();
                if(response.code() == 404) {
                    log.warn("This functionality is not available for your Zebrunner distribution");
                } else {
                    log.error(this.formatError("Could not send test case results.", response));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.uploadScreenshotCall(token, capturedAt.toString(), testRunId.toString(),
                    testId.toString(), screenshot);
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    log.error(this.formatError("Could not upload a screenshot.", response));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void uploadTestRunArtifact(InputStream artifact, String name, Long testRunId) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            try {
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", name, RequestBody.create(artifact.readAllBytes()));
                Call<String> call = client.uploadTestRunArtifactCall(token, testRunId.toString(), filePart);
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    log.error(this.formatError("Could not attach test run artifact with name " + name, response));
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read artifact bytes or execute response", e);
            }
        });
    }

    @Override
    public void uploadTestArtifact(InputStream artifact, String name, Long testRunId, Long testId) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            try {
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", name, RequestBody.create(artifact.readAllBytes()));
                Call<String> call = client.uploadTestArtifactCall(token, testRunId.toString(), testId.toString(), filePart);
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    log.error(this.formatError("Could not attach test artifact with name " + name, response));
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read artifact bytes or execute response", e);
            }
        });
    }

    @Override
    public void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReferenceDTO artifactReference) {
        String token = obtainToken();
        Map<String, List<ArtifactReferenceDTO>> requestBody = Collections.singletonMap(
                "items", Collections.singletonList(artifactReference)
        );
        this.sendVoidRequest(client -> {
            Call<String> call = client.attachArtifactReferenceToTestRunCall(token, testRunId.toString(), requestBody);
            try {
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    log.error(this.formatError(
                                "Could not attach the following test run artifact reference: " + artifactReference,
                                response
                        ));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReferenceDTO artifactReference) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.attachArtifactReferenceToTestCall(token, testRunId.toString(), testId.toString(),
                    Collections.singletonMap("items", Collections.singletonList(artifactReference)));
            try {
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    log.error(this.formatError(
                                "Could not attach the following test artifact reference: " + artifactReference,
                                response
                        ));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void attachLabelsToTestRun(Long testRunId, Collection<LabelDTO> labels) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.attachLabelsToTestRunCall(token, testRunId.toString(), Collections.singletonMap("items", labels));
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    log.error(this.formatError("Could not attach the following labels to test run: " + labels, response));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void attachLabelsToTest(Long testRunId, Long testId, Collection<LabelDTO> labels) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.attachLabelsToTestCall(token, testRunId.toString(), testId.toString(),
                    Collections.singletonMap("items", labels));
            try {
                Response<String> response = call.execute();
                if(!response.isSuccessful()) {
                    log.error(this.formatError("Could not attach the following labels to test: " + labels, response));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ExchangeRunContextResponse exchangeRerunCondition(String rerunCondition) {
        String token = obtainToken();
        return this.sendRequest(client -> {
            Call<ExchangeRunContextResponse> call = client.exchangeRerunConditionCall(token, rerunCondition);
            try {
                Response<ExchangeRunContextResponse> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not get tests by ci run id.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public TestSessionDTO startSession(Long testRunId, TestSessionDTO testSession) {
        String token = obtainToken();
        return this.sendRequest(client -> {
            Call<TestSessionDTO> call = client.startSessionCall(token, testRunId.toString(), testSession);
            try {
                Response<TestSessionDTO> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not register start of the test session.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateSession(Long testRunId, TestSessionDTO testSession) {
        String token = obtainToken();
        this.sendVoidRequest(client -> {
            Call<String> call = client.updateSessionCall(token, testRunId.toString(), testSession.getId().toString(), testSession);
            try {
                Response<String> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not update test session.", response);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public boolean isKnownIssueAttachedToTest(Long testRunId, Long testId, String failureStacktrace) {
        String token = obtainToken();
        KnownIssueConfirmation confirmation = this.sendRequest(client -> {
            Call<KnownIssueConfirmation> call = client.isKnownIssueAttachedToTestCall(token, testRunId.toString(), testId.toString(),
                    Collections.singletonMap("failureReason", failureStacktrace));
            try {
                Response<KnownIssueConfirmation> response = call.execute();
                if (!response.isSuccessful()) {
                    this.throwServerException("Could not retrieve status of attached known issues.", response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return confirmation != null && confirmation.isKnownIssue();
    }

    private String obtainToken() {
        if (token == null) {
            AutenticationData autenticationData = this.login();
            this.token = "Bearer " + autenticationData.getAuthToken();
        }
        return token;
    }

    private AutenticationData login() {
        return getAuthData();
    }

    private AutenticationData getAuthData() {
        String refreshToken = ConfigurationHolder.getToken();
        Call<AutenticationData> call = client.getAuthData(Collections.singletonMap("refreshToken", refreshToken));
        try {
            Response<AutenticationData> response = call.execute();
            if (!response.isSuccessful()) {
                // null out the api client since we cannot use it anymore
                client = null;
                this.throwServerException("Not able to obtain api token", response);
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
