package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
public class RetrofitZebrunnerApiClient implements ZebrunnerApiClient{

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrofitZebrunnerApiClient.class);

    private static RetrofitZebrunnerApiClient INSTANCE;

    private RetrofitZebrunnerApiClient() {}

    static synchronized RetrofitZebrunnerApiClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RetrofitZebrunnerApiClient();
        }
        return INSTANCE;
    }
    @Override
    public TestRunDTO registerTestRunStart(TestRunDTO testRun) {
        System.out.println("register test run start");
        return null;
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
        System.out.println("registerTestRunFinish");
    }

    @Override
    public TestDTO registerTestStart(Long testRunId, TestDTO test, boolean headless) {
        System.out.println("registerTestStart");
        return null;
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
        System.out.println("registerTestFinish");
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
