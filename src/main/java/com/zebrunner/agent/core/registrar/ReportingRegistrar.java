package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.ci.CiContextResolver;
import com.zebrunner.agent.core.registrar.ci.CompositeCiContextResolver;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.domain.NotificationTargetDTO;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.core.registrar.domain.TestRunDTO;
import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

@Slf4j
class ReportingRegistrar implements TestRunRegistrar {

    private static final String TEST_RUN_WARNING_MSG_FORMAT = "[TEST RUN '{}' WARNING]: {}";

    private static volatile ReportingRegistrar instance;

    private static final List<LogsBuffer<?>> LOGS_BUFFERS = new Vector<>();

    public static ReportingRegistrar getInstance() {
        if (instance == null) {
            RerunResolver.resolve();
            instance = new ReportingRegistrar();
        }
        return instance;
    }

    private final ZebrunnerApiClient apiClient = ClientRegistrar.getClient();
    private final CompositeLabelResolver labelResolver = new CompositeLabelResolver();
    private final ChainedMaintainerResolver maintainerResolver = new ChainedMaintainerResolver();
    private final CiContextResolver ciContextResolver = CompositeCiContextResolver.getInstance();
    private final TestSessionRegistrar testSessionRegistrar = TestSessionRegistrar.getInstance();
    private final RegistrationListenerRegistry registrationListenerRegistry = RegistrationListenerRegistry.getInstance();

    public static void registerLogsBuffer(LogsBuffer<?> logsBuffer) {
        LOGS_BUFFERS.add(logsBuffer);
    }

    @Override
    public void registerStart(TestRunStartDescriptor tr) {
        registrationListenerRegistry.forEach(listener -> listener.onBeforeTestRunStart(tr));

        TestRunDTO testRun = TestRunDTO.builder()
                                       .uuid(RunContextHolder.getTestRunUuid())
                                       .name(ConfigurationHolder.getRunDisplayNameOr(tr.getName()))
                                       .framework(tr.getFramework())
                                       .startedAt(tr.getStartedAt())
                                       .config(new TestRunDTO.Config(
                                         ConfigurationHolder.getRunEnvironment(),
                                         ConfigurationHolder.getRunBuild(),
                                         ConfigurationHolder.shouldTreatSkipsAsFailures()
                                 ))
                                       .jenkinsContext(new TestRunDTO.JenkinsContext(
                                         System.getProperty("ci_url"),
                                         getIntegerSystemProperty("ci_build"),
                                         System.getProperty("ci_parent_url"),
                                         getIntegerSystemProperty("ci_parent_build")
                                 ))
                                       .ciContext(ciContextResolver.resolve())
                                       .milestone(new TestRunDTO.Milestone(
                                         ConfigurationHolder.getMilestoneId(),
                                         ConfigurationHolder.getMilestoneName()
                                 ))
                                       .notifications(new TestRunDTO.Notifications(
                                         ConfigurationHolder.notificationsEnabled(),
                                         collectNotificationTargets(),
                                         ConfigurationHolder.shouldNotifyOnEachFailure()
                                 ))
                                       .build();
        testRun = apiClient.registerTestRunStart(testRun);

        // if reporting is enabled and test run was actually registered
        if (testRun != null) {
            TestRunDescriptor testRunDescriptor = TestRunDescriptor.create(testRun.getId(), tr);
            RunContext.setRun(testRunDescriptor);
            logTestRunWarnings(testRun);
            saveRunLocaleFromProgramArguments();
            registrationListenerRegistry.forEach(listener -> listener.onAfterTestRunStart(tr));
        }
    }

    private void logTestRunWarnings(TestRunDTO testRun) {
        TestRunDTO.Metadata metadata = testRun.getMetadata();
        if (metadata != null) {
            String testRunName = testRun.getName();
            metadata.getWarningMessages()
                    .forEach(warning -> log.warn(TEST_RUN_WARNING_MSG_FORMAT, testRunName, warning));
        }
    }

    private Integer getIntegerSystemProperty(String propertyName) {
        return Optional.ofNullable(System.getProperty(propertyName))
                       .filter(parentBuild -> !parentBuild.isEmpty())
                       .map(Integer::parseInt)
                       .orElse(null);
    }

    private void saveRunLocaleFromProgramArguments() {
        String locale = System.getProperty("locale");
        if (locale != null) {
            Label.attachToTestRun(Label.LOCALE, locale);
        }
    }

    private Set<NotificationTargetDTO> collectNotificationTargets() {
        Set<NotificationTargetDTO> notificationTargets = new HashSet<>();

        String slackChannels = ConfigurationHolder.getSlackChannels();
        if (slackChannels != null && !slackChannels.isEmpty()) {
            notificationTargets
                    .add(new NotificationTargetDTO(NotificationTargetDTO.Type.SLACK_CHANNELS, slackChannels));
        }

        String msTeamsChannels = ConfigurationHolder.getMsTeamsChannels();
        if (msTeamsChannels != null && !msTeamsChannels.isEmpty()) {
            notificationTargets
                    .add(new NotificationTargetDTO(NotificationTargetDTO.Type.MS_TEAMS_CHANNELS, msTeamsChannels));
        }

        String emailRecipients = ConfigurationHolder.getEmails();
        if (emailRecipients != null && !emailRecipients.isEmpty()) {
            notificationTargets
                    .add(new NotificationTargetDTO(NotificationTargetDTO.Type.EMAIL_RECIPIENTS, emailRecipients));
        }

        return notificationTargets;
    }

    @Override
    public void registerFinish(TestRunFinishDescriptor finishDescriptor) {
        TestRunDTO testRun = TestRunDTO.builder()
                                       .id(RunContext.getZebrunnerRunId())
                                       .endedAt(finishDescriptor.getEndedAt())
                                       .build();
        apiClient.registerTestRunFinish(testRun);

        TestRunDescriptor run = RunContext.getRun();
        if (run != null) {
            run.complete(finishDescriptor);
        }
    }

    @Override
    public void registerHeadlessTestStart(String id, TestStartDescriptor ts) {
        if (!RunContext.getCurrentTest().isPresent()) { // we should not register the same headless test twice
            TestDTO test = TestDTO.builder()
                                  .name(ts.getName())
                                  .startedAt(ts.getStartedAt())
                                  .build();

            Long zebrunnerRunId = RunContext.getZebrunnerRunId();
            if (ts.getZebrunnerId() != null) {
                test = apiClient.registerTestRerunStart(zebrunnerRunId, ts.getZebrunnerId(), test, true);
            } else {
                test = apiClient.registerTestStart(zebrunnerRunId, test, true);
            }

            // if reporting is enabled and test was actually registered
            if (test != null) {
                TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
                RunContext.addCurrentTest(id, testDescriptor);
                testSessionRegistrar.linkAllCurrentToTest(test.getId());
            }
            LOGS_BUFFERS.forEach(LogsBuffer::flushQueuedConfigurationLogs);
        }
    }

    @Override
    public void registerTestStart(String id, TestStartDescriptor ts) {
        registrationListenerRegistry.forEach(listener -> listener.onBeforeTestStart(ts));

        TestDTO test = TestDTO.builder()
                              .correlationData(ts.getCorrelationData())
                              .name(ts.getName())
                              .className(ts.getTestClassName() != null ? ts.getTestClassName() : ts.getTestClass().getName())
                              .methodName(ts.getTestMethodName() != null ? ts.getTestMethodName() : ts.getTestMethod().getName())
                              .argumentsIndex(ts.getArgumentsIndex())
                              .maintainer(maintainerResolver.resolve(ts.getTestClass(), ts.getTestMethod()))
                              .startedAt(ts.getStartedAt())
                              .labels(labelResolver.resolve(ts.getTestClass(), ts.getTestMethod()))
                              .testGroups(ts.getTestGroups())
                              .build();

        Long headlessTestId = RunContext.getCurrentTest()
                                        .map(TestDescriptor::getZebrunnerId)
                                        .orElse(null);
        if (headlessTestId != null) {
            test.setId(headlessTestId);
            test = apiClient.registerHeadlessTestUpdate(RunContext.getZebrunnerRunId(), test);
        } else if (ts.getZebrunnerId() != null) {
            test = apiClient.registerTestRerunStart(RunContext.getZebrunnerRunId(), ts.getZebrunnerId(), test, false);
        } else {
            test = apiClient.registerTestStart(RunContext.getZebrunnerRunId(), test, false);
        }

        // if reporting is enabled and test was actually registered
        if (test != null) {
            TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
            RunContext.addCurrentTest(id, testDescriptor);
            testSessionRegistrar.linkAllCurrentToTest(test.getId());
            registrationListenerRegistry.forEach(listener -> listener.onAfterTestStart(ts));
        }
        LOGS_BUFFERS.forEach(LogsBuffer::flushQueuedConfigurationLogs);
    }

    @Override
    public boolean isTestStarted() {
        return RunContext.getCurrentTest().isPresent();
    }

    @Override
    public boolean isTestStarted(String id) {
        return RunContext.getTest(id) != null;
    }

    @Override
    public void registerTestFinish(String id, TestFinishDescriptor tf) {
        TestDescriptor test = RunContext.getTest(id);
        if (test != null) {
            registrationListenerRegistry.forEach(listener -> listener.onBeforeTestFinish(tf));

            TestDTO result = TestDTO.builder()
                                    .id(test.getZebrunnerId())
                                    .result(tf.getStatus().name())
                                    .reason(tf.getStatusReason())
                                    .endedAt(tf.getEndedAt())
                                    .build();

            apiClient.registerTestFinish(RunContext.getZebrunnerRunId(), result);

            registrationListenerRegistry.forEach(listener -> listener.onAfterTestFinish(tf));
            RunContext.completeTest(id, tf);
        }
    }

    @Override
    public void registerAfterTestStart() {
        RunContext.startAfterMethod();
    }

    @Override
    public void registerAfterTestFinish() {
        RunContext.finishAfterMethod();
    }

    @Override
    public boolean isKnownIssueAttachedToTest(String failureStacktrace) {
        Long runId = RunContext.getZebrunnerRunId();
        Optional<Long> maybeTestId = RunContext.getCurrentTest().map(TestDescriptor::getZebrunnerId);
        if (maybeTestId.isPresent()) {
            Long testId = maybeTestId.get();
            return apiClient.isKnownIssueAttachedToTest(runId, testId, failureStacktrace);
        } else {
            log.error("Failed to retrieve assigned known issues for stacktrace '{}' because test has not been started yet.", failureStacktrace);
            return false;
        }
    }

    @Override
    public void clearConfigurationLogs() {
        LOGS_BUFFERS.forEach(LogsBuffer::clearQueuedConfigurationLogs);
    }

}
