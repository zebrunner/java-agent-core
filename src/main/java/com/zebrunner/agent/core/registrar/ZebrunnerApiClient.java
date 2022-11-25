package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.domain.ArtifactReferenceDTO;
import com.zebrunner.agent.core.registrar.domain.ExchangeRunContextResponse;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;
import com.zebrunner.agent.core.registrar.domain.TestCaseResult;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.core.registrar.domain.TestRunDTO;
import com.zebrunner.agent.core.registrar.domain.TestSessionDTO;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;


import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public interface ZebrunnerApiClient {

    static  ZebrunnerApiClient getInstance() {
        return ClientRegistrar.getClient();
    }

    TestRunDTO registerTestRunStart(TestRunDTO testRun);

    void patchTestRunBuild(Long testRunId, String build);

    void setTestRunPlatform(Long testRunId, String platformName, String platformVersion);

    void registerTestRunFinish(TestRunDTO testRun);

    TestDTO registerTestStart(Long testRunId, TestDTO test, boolean headless);

    TestDTO registerTestRerunStart(Long testRunId, Long testId, TestDTO test, boolean headless);

    TestDTO registerHeadlessTestUpdate(Long testRunId, TestDTO test);

    void revertTestRegistration(Long testRunId, Long testId);

    void registerTestFinish(Long testRunId, TestDTO test);

    void sendLogs(Collection<Log> logs, Long testRunId);

    void upsertTestCaseResults(Long testRunId, Long testId, Collection<TestCaseResult> testCaseResults);

    void uploadScreenshot(byte[] screenshot, Long testRunId, Long testId, Long capturedAt);

    void uploadTestRunArtifact(InputStream artifact, String name, Long testRunId);

    void uploadTestArtifact(InputStream artifact, String name, Long testRunId, Long testId);

    void attachArtifactReferenceToTestRun(Long testRunId, ArtifactReferenceDTO artifactReference);

    void attachArtifactReferenceToTest(Long testRunId, Long testId, ArtifactReferenceDTO artifactReference);

    void attachLabelsToTestRun(Long testRunId, Collection<LabelDTO> labels);

    void attachLabelsToTest(Long testRunId, Long testId, Collection<LabelDTO> labels);

    ExchangeRunContextResponse exchangeRerunCondition(String rerunCondition);

    TestSessionDTO startSession(Long testRunId, TestSessionDTO testSession);

    void updateSession(Long testRunId, TestSessionDTO testSession);

    boolean isKnownIssueAttachedToTest(Long testRunId, Long testId, String failureStacktrace);

}
