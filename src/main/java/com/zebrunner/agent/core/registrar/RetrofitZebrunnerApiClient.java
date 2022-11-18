package com.zebrunner.agent.core.registrar;


import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.exception.ServerException;
import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.domain.*;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Thread.sleep;

public class RetrofitZebrunnerApiClient implements ZebrunnerApiClient {

    private HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static final String apiHost = ConfigurationHolder.getHost();
    private static RetrofitZebrunnerApiClient INSTANCE;
    private static Gson gson = Converters.registerOffsetDateTime(new GsonBuilder()).create();
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(apiHost)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson));
    private static Retrofit retrofit = builder.build();
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private volatile ApiClientService client;
    private volatile AutenticationData autenticationData;
    private volatile String token;

    private RetrofitZebrunnerApiClient() {
        if (ConfigurationHolder.isReportingEnabled()) {
            client = initClient();
            setAutenticationData();
            try {
                sleep(7000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            client = authenticateClient(ApiClientService.class);
        }
    }

    static synchronized RetrofitZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RetrofitZebrunnerApiClient();
        }
        return INSTANCE;
    }

    private ApiClientService initClient() {
        return retrofit.create(ApiClientService.class);
    }

    private ApiClientService authenticateClient(Class<ApiClientService> serviceClass) {
        httpClient.interceptors()
                  .clear();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                                      .header("Authorization", token)
                                      .addHeader("Connection", "close")
                                      .addHeader("Content-Type", "application/json")
                                      .addHeader("Accept", "application/json")
                                      .build();
            return chain.proceed(request);
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        builder.client(httpClient.build());
        retrofit = builder.build();
        return retrofit.create(serviceClass);
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

    private void setAutenticationData() {
        String refreshToken = ConfigurationHolder.getToken();
        Call<AutenticationData> call = client.getAuthData(Collections.singletonMap("refreshToken", refreshToken));
        call.enqueue(new Callback<AutenticationData>() {
            @Override
            public void onResponse(Call<AutenticationData> call, Response<AutenticationData> response) {
                if (!response.isSuccessful()) {
                    // null out the api client since we cannot use it anymore
                    client = null;
                    throw new ServerException("Not able to obtain api token");
                }
                autenticationData = response.body();
                token = autenticationData.getAuthTokenType() + " " + autenticationData.getAuthToken();
            }

            @Override
            public void onFailure(Call<AutenticationData> call, Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    @Override
    public TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        return this.sendRequest(client -> {
            Call<TestRunDTO> call = client.getTestRunDTO(testRun, ConfigurationHolder.getProjectKey());
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

    @Override
    public void patchTestRunBuild(Long testRunId, String build) {
        System.out.println("patchTestRunBuild");
    }

    @Override
    public void setTestRunPlatform(Long testRunId, String platformName, String platformVersion) {
        System.out.println("setTestRunPlatform");
    }

    @Override
    public void registerTestRunFinish(TestRunDTO testRun) {
        this.sendVoidRequest(client -> {
            Call<String> call = client.callTestRunFinish(testRun, testRun.getId().toString());
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
        return this.sendRequest(client -> {
            Call<TestDTO> call = client.getTestDTO(testRunId.toString(), headless, test);
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
        System.out.println("registerTestRerunStart");
        return null;
    }

    @Override
    public TestDTO registerHeadlessTestUpdate(Long testRunId, TestDTO test) {
        System.out.println("registerHeadlessTestUpdate");
        return null;
    }

    @Override
    public void revertTestRegistration(Long testRunId, Long testId) {
        System.out.println("revertTestRegistration");
    }

    @Override
    public void registerTestFinish(Long testRunId, TestDTO test) {
        this.sendVoidRequest(client -> {
            Call<String> call = client.callTestFinish(testRunId.toString(), test.getId()
                                                                                .toString(), false, test);
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
        System.out.println("sendLogs");
    }

    @Override
    public void upsertTestCaseResults(Long testRunId, Long testId, Collection<TestCaseResult> testCaseResults) {
        System.out.println("upsertTestCaseResults");
    }

    @Override
    public void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt) {
        System.out.println("uploadScreenshot");
    }

    @Override
    public void uploadTestRunArtifact(InputStream artifact, String name, Long testRunId) {
        System.out.println("uploadTestRunArtifact");
    }

    @Override
    public void uploadTestArtifact(InputStream artifact, String name, Long testRunId, Long testId) {
        System.out.println("uploadTestArtifact");
    }

    @Override
    public void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReferenceDTO artifactReference) {
        System.out.println("attachArtifactReferenceToTestRun");
    }

    @Override
    public void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReferenceDTO artifactReference) {
        System.out.println("attachArtifactReferenceToTest");
    }

    @Override
    public void attachLabelsToTestRun(Long testRunId, Collection<LabelDTO> labels) {
        System.out.println("attachLabelsToTestRun");
    }

    @Override
    public void attachLabelsToTest(Long testRunId, Long testId, Collection<LabelDTO> labels) {
        System.out.println("attachLabelsToTest");
    }

    @Override
    public ExchangeRunContextResponse exchangeRerunCondition(String rerunCondition) {
        System.out.println("exchangeRerunCondition");
        return null;
    }

    @Override
    public TestSessionDTO startSession(Long testRunId, TestSessionDTO testSession) {
        System.out.println("startSession");
        return null;
    }

    @Override
    public void updateSession(Long testRunId, TestSessionDTO testSession) {
        System.out.println("updateSession");
    }

    @Override
    public boolean isKnownIssueAttachedToTest(Long testRunId, Long testId, String failureStacktrace) {
        System.out.println("isKnownIssueAttachedToTest");
        return false;
    }
}
