package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Vector;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.ci.CiContextResolver;
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.client.request.FinishTestRequest;
import com.zebrunner.agent.core.registrar.client.request.FinishTestRunRequest;
import com.zebrunner.agent.core.registrar.client.request.StartHeadlessTestRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestRequest;
import com.zebrunner.agent.core.registrar.client.request.StartTestRunRequest;
import com.zebrunner.agent.core.registrar.client.response.StartTestResponse;
import com.zebrunner.agent.core.registrar.client.response.StartTestRunResponse;
import com.zebrunner.agent.core.registrar.domain.TestFinish;
import com.zebrunner.agent.core.registrar.domain.TestRunFinish;
import com.zebrunner.agent.core.registrar.domain.TestRunStart;
import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.domain.Label;
import com.zebrunner.agent.core.registrar.domain.Test;
import com.zebrunner.agent.core.registrar.domain.TestRun;
import com.zebrunner.agent.core.registrar.domain.TestSession;
import com.zebrunner.agent.core.registrar.label.LabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReportingRegistrar implements TestRunRegistrar {

    private static final List<LogsBuffer<?>> LOGS_BUFFERS = new Vector<>();

    private static volatile ReportingRegistrar instance;

    public static synchronized ReportingRegistrar getInstance() {
        if (instance == null) {
            RunContextService.resolveRunContext();
            instance = new ReportingRegistrar();
        }
        return instance;
    }

    private final ZebrunnerApiClient apiClient = ApiClientRegistry.getClient();
    private final LabelResolver labelResolver = LabelResolver.getInstance();
    private final CiContextResolver ciContextResolver = CiContextResolver.getInstance();
    private final MaintainerResolver maintainerResolver = MaintainerResolver.getInstance();
    private final TestSessionRegistrar testSessionRegistrar = TestSessionRegistrar.getInstance();
    private final RegistrationListenerRegistry registrationListenerRegistry = RegistrationListenerRegistry.getInstance();

    public static void registerLogsBuffer(LogsBuffer<?> logsBuffer) {
        LOGS_BUFFERS.add(logsBuffer);
    }

    @Override
    public void registerStart(TestRunStart testRunStart) {
        registrationListenerRegistry.forEach(listener -> listener.onBeforeTestRunStart(testRunStart));

        var request = new StartTestRunRequest().setUuid(RunContextService.getTestRunUuid())
                                               .setName(ConfigurationHolder.getRunDisplayNameOr(testRunStart.getName()))
                                               .setFramework(testRunStart.getFramework())
                                               .setStartedAt(testRunStart.getStartedAt())
                                               .setConfig(new StartTestRunRequest.Config(
                                                       ConfigurationHolder.getRunEnvironment(),
                                                       ConfigurationHolder.getRunBuild(),
                                                       ConfigurationHolder.shouldTreatSkipsAsFailures()
                                               ))
                                               .setCiContext(ciContextResolver.resolve())
                                               .setMilestone(new StartTestRunRequest.Milestone(
                                                       ConfigurationHolder.getMilestoneId(),
                                                       ConfigurationHolder.getMilestoneName()
                                               ))
                                               .setNotifications(new StartTestRunRequest.Notifications(
                                                       ConfigurationHolder.notificationsEnabled(),
                                                       ConfigurationHolder.collectNotificationTargets(),
                                                       ConfigurationHolder.shouldNotifyOnEachFailure()
                                               ));

        StartTestRunResponse response = apiClient.registerTestRunStart(request);

        // if reporting is enabled and test run was actually registered
        if (response != null) {
            ReportingContext.setTestRun(TestRun.of(response));

            String locale = System.getProperty("locale");
            if (locale != null) {
                CurrentTestRun.setLocale(locale);
            }

            registrationListenerRegistry.forEach(listener -> listener.onAfterTestRunStart(testRunStart));
        }
    }

    @Override
    public void registerFinish(TestRunFinish testRunFinish) {
        var request = new FinishTestRunRequest().setEndedAt(testRunFinish.getEndedAt());

        ReportingContext.getTestRun()
                        .ifPresent(testRun -> {
                            apiClient.registerTestRunFinish(testRun.getId(), request);
                            testRun.ended(testRunFinish.getEndedAt());
                        });
    }

    @Override
    public void registerHeadlessTestStart(String id, TestStart testStart) {
        if (ReportingContext.getCurrentTest().isEmpty()) { // we should not register the same headless test twice
            Long testRunId = ReportingContext.getNullableTestRunId();
            if (testRunId == null) {
                return;
            }

            var request = new StartHeadlessTestRequest().setName(testStart.getName())
                                                        .setStartedAt(testStart.getStartedAt());

            StartTestResponse response;
            if (testStart.getId() != null) {
                response = apiClient.registerHeadlessTestRerunStart(testRunId, testStart.getId(), request);
            } else {
                response = apiClient.registerHeadlessTestStart(testRunId, request);
            }

            // if reporting is enabled and test was actually registered
            if (response != null) {
                Test test = Test.of(response, testStart);

                ReportingContext.addCurrentTest(id, test);
                testSessionRegistrar.linkAllCurrentToTest(test.getId());
            }
            LOGS_BUFFERS.forEach(LogsBuffer::flushQueuedConfigurationLogs);
        }
    }

    @Override
    public void registerTestStart(String id, TestStart testStart) {
        registrationListenerRegistry.forEach(listener -> listener.onBeforeTestStart(testStart));

        TestRun testRun = ReportingContext.getNullableTestRun();
        if (testRun == null) {
            return;
        }

        List<TestSession> testSessions = ReportingContext.getCurrentTestSessions();
        var request = new StartTestRequest().setName(testStart.getName())
                                            .setCorrelationData(testStart.getCorrelationData())
                                            .setClassName(testStart.getTestClassName() != null
                                                    ? testStart.getTestClassName()
                                                    : testStart.getTestClass().getName())
                                            .setMethodName(testStart.getTestMethodName() != null
                                                    ? testStart.getTestMethodName()
                                                    : testStart.getTestMethod().getName())
                                            .setArgumentsIndex(testStart.getArgumentsIndex())
                                            .setMaintainer(maintainerResolver.resolve(testRun, testStart, testSessions))
                                            .setStartedAt(testStart.getStartedAt())
                                            .setLabels((List<Label>) labelResolver.resolve(testStart.getTestClass(), testStart.getTestMethod()))
                                            .setTestGroups(testStart.getTestGroups());

        StartTestResponse response = this.registerTestStart(testRun.getId(), testStart.getId(), request);

        // if reporting is enabled and test was actually registered
        if (response != null) {
            Test test = Test.of(response, testStart);

            ReportingContext.addCurrentTest(id, test);
            testSessionRegistrar.linkAllCurrentToTest(test.getId());

            registrationListenerRegistry.forEach(listener -> listener.onAfterTestStart(testStart));
        }
        LOGS_BUFFERS.forEach(LogsBuffer::flushQueuedConfigurationLogs);
    }

    private StartTestResponse registerTestStart(Long testRunId, Long nullableTestId, StartTestRequest request) {
        Long headlessTestId = ReportingContext.getCurrentTest()
                                              .map(Test::getId)
                                              .orElse(null);

        if (headlessTestId != null) {
            return apiClient.registerHeadlessTestUpdate(testRunId, headlessTestId, request);
        } else if (nullableTestId != null) {
            return apiClient.registerTestRerunStart(testRunId, nullableTestId, request);
        } else {
            return apiClient.registerTestStart(testRunId, request);
        }
    }

    @Override
    public boolean isTestStarted() {
        return ReportingContext.getCurrentTest().isPresent();
    }

    @Override
    public boolean isTestStarted(String id) {
        return ReportingContext.getTest(id) != null;
    }

    @Override
    public void registerTestFinish(String id, TestFinish testFinish) {
        Long testRunId = ReportingContext.getNullableTestRunId();
        if (testRunId == null) {
            return;
        }

        Test test = ReportingContext.getTest(id);

        if (test != null) {
            registrationListenerRegistry.forEach(listener -> listener.onBeforeTestFinish(testFinish));

            var request = new FinishTestRequest().setEndedAt(testFinish.getEndedAt())
                                                 .setResult(testFinish.getStatus().name())
                                                 .setReason(testFinish.getStatusReason());

            apiClient.registerTestFinish(testRunId, test.getId(), request);

            registrationListenerRegistry.forEach(listener -> listener.onAfterTestFinish(testFinish));
            ReportingContext.completeTest(id, testFinish);
        }
    }

    @Override
    public void registerAfterTestStart() {
        ReportingContext.startAfterMethod();
    }

    @Override
    public void registerAfterTestFinish() {
        ReportingContext.finishAfterMethod();
    }

    @Override
    public boolean isKnownIssueAttachedToTest(String failureStacktrace) {
        Long testRunId = ReportingContext.getNullableTestRunId();
        if (testRunId == null) {
            return false;
        }

        return ReportingContext.getCurrentTest()
                               .map(Test::getId)
                               .map(testId -> apiClient.isKnownIssueAttachedToTest(testRunId, testId, failureStacktrace))
                               .orElseGet(() -> {
                                   log.error("Failed to retrieve assigned known issues for stacktrace '{}' because test has not been started yet.", failureStacktrace);
                                   return false;
                               });
    }

    @Override
    public void clearConfigurationLogs() {
        LOGS_BUFFERS.forEach(LogsBuffer::clearQueuedConfigurationLogs);
    }

}
