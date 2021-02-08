package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.core.registrar.domain.TestRunDTO;
import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
class ReportingRegistrar implements TestRunRegistrar {

    static {
        RerunResolver.resolve();
    }

    private static final String CI_RUN_ID = System.getProperty("ci_run_id");
    private static volatile ReportingRegistrar instance;

    public static ReportingRegistrar getInstance() {
        if (instance == null) {
            instance = new ReportingRegistrar();
        }
        return instance;
    }

    private final ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();
    private final CompositeLabelResolver labelResolver = new CompositeLabelResolver();
    private final DriverSessionRegistrar driverSessionRegistrar = DriverSessionRegistrar.getInstance();
    private final ChainedMaintainerResolver maintainerResolver = new ChainedMaintainerResolver();

    @Override
    public void registerStart(TestRunStartDescriptor tr) {
        log.info("Ci run id = '{}'", CI_RUN_ID);
        TestRunDTO testRun = TestRunDTO.builder()
                                       .uuid(Optional.ofNullable(RerunResolver.getRunId()).orElse(CI_RUN_ID))
                                       .name(ConfigurationHolder.getRunDisplayNameOr(tr.getName()))
                                       .framework(tr.getFramework())
                                       .startedAt(tr.getStartedAt())
                                       .config(new TestRunDTO.Config(
                                               ConfigurationHolder.getRunEnvironment(),
                                               ConfigurationHolder.getRunBuild()
                                       ))
                                       .build();

        testRun = apiClient.registerTestRunStart(testRun);

        // if reporting is enabled and test run was actually registered
        if (testRun != null) {
            TestRunDescriptor testRunDescriptor = TestRunDescriptor.create(testRun.getId(), tr);
            RunContext.setRun(testRunDescriptor);
        }
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
        TestDTO test = TestDTO.builder()
                              .name(ts.getName())
                              .startedAt(ts.getStartedAt())
                              .build();

        test = apiClient.registerTestStart(RunContext.getZebrunnerRunId(), test, true);

        // if reporting is enabled and test was actually registered
        if (test != null) {
            TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
            RunContext.addTest(id, testDescriptor);
            driverSessionRegistrar.linkAllCurrentToTest(test.getId());
        }
    }

    @Override
    public void registerTestStart(String id, TestStartDescriptor ts) {
        TestDTO test = TestDTO.builder()
                              .uuid(ts.getUuid())
                              .name(ts.getName())
                              .className(ts.getTestClass().getName())
                              .methodName(ts.getTestMethod().getName())
                              .maintainer(maintainerResolver.resolve(ts.getTestClass(), ts.getTestMethod()))
                              .startedAt(ts.getStartedAt())
                              .labels(labelResolver.resolve(ts.getTestClass(), ts.getTestMethod()))
                              .build();

        Long headlessTestId = RunContext.getCurrentTest()
                                        .map(TestDescriptor::getZebrunnerId)
                                        .orElse(null);
        if (headlessTestId != null) {
            test.setId(headlessTestId);
            test = apiClient.registerHeadlessTestUpdate(RunContext.getZebrunnerRunId(), test);
        } else {
            test = apiClient.registerTestStart(RunContext.getZebrunnerRunId(), test, false);
        }

        // if reporting is enabled and test was actually registered
        if (test != null) {
            TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
            RunContext.addTest(id, testDescriptor);
            driverSessionRegistrar.linkAllCurrentToTest(test.getId());
        }
    }

    @Override
    public boolean isTestStarted(String id) {
        return RunContext.getTest(id) != null;
    }

    @Override
    public void registerTestFinish(String id, TestFinishDescriptor tf) {
        TestDescriptor test = RunContext.getTest(id);
        if (test != null) {
            TestDTO result = TestDTO.builder()
                                    .id(test.getZebrunnerId())
                                    .result(tf.getStatus().name())
                                    .reason(tf.getStatusReason())
                                    .endedAt(tf.getEndedAt())
                                    .build();

            apiClient.registerTestFinish(RunContext.getZebrunnerRunId(), result);

            RunContext.completeTest(id, tf);
        }
    }

    private static final class TestRunBuilder {

        private static final int SLACK_CHANNELS_LIMIT = 20;
        private static final int EMAIL_RECIPIENTS_LIMIT = 20;
        private static final int MICROSOFT_TEAMS_CHANNELS_LIMIT = 20;

        private static final String SLACK_CHANNELS_NOTIFICATION_TYPE = "SLACK_CHANNELS";
        private static final String EMAIL_RECIPIENTS_NOTIFICATION_TYPE = "EMAIL_RECIPIENTS";
        private static final String MICROSOFT_TEAMS_CHANNELS_NOTIFICATION_TYPE = "MS_TEAMS_CHANNELS";

        public static TestRunDTO build(TestRunStartDescriptor testRunStartDescriptor) {
            TestRunDTO.TestRunDTOBuilder testRunBuilder = TestRunDTO.builder()
                                                                    .uuid(Optional.ofNullable(RerunResolver.getRunId()).orElse(CI_RUN_ID))
                                                                    .name(ConfigurationHolder.getRunDisplayNameOr(testRunStartDescriptor.getName()))
                                                                    .framework(testRunStartDescriptor.getFramework())
                                                                    .startedAt(testRunStartDescriptor.getStartedAt())
                                                                    .config(new TestRunDTO.Config(
                                                                            ConfigurationHolder.getRunEnvironment(),
                                                                            ConfigurationHolder.getRunBuild()
                                                                    ));

            Set<TestRunDTO.NotificationDTO> slackChannels = ConfigurationHolder.getSlackChannels()
                                                                               .stream()
                                                                               .limit(SLACK_CHANNELS_LIMIT)
                                                                               .map(channel -> new TestRunDTO.NotificationDTO(SLACK_CHANNELS_NOTIFICATION_TYPE, channel))
                                                                               .collect(Collectors.toSet());

            Set<TestRunDTO.NotificationDTO> emails = ConfigurationHolder.getEmails()
                                                                        .stream()
                                                                        .limit(EMAIL_RECIPIENTS_LIMIT)
                                                                        .map(recipient -> new TestRunDTO.NotificationDTO(EMAIL_RECIPIENTS_NOTIFICATION_TYPE, recipient))
                                                                        .collect(Collectors.toSet());

            Set<TestRunDTO.NotificationDTO> msTeamsChannels = ConfigurationHolder.getMicrosoftTeamsChannels()
                                                                                 .stream()
                                                                                 .limit(MICROSOFT_TEAMS_CHANNELS_LIMIT)
                                                                                 .map(channel -> new TestRunDTO.NotificationDTO(MICROSOFT_TEAMS_CHANNELS_NOTIFICATION_TYPE, channel))
                                                                                 .collect(Collectors.toSet());

            Set<TestRunDTO.NotificationDTO> notificationTargets = Stream.of(slackChannels, emails, msTeamsChannels)
                                                                        .flatMap(Set::stream)
                                                                        .collect(Collectors.toSet());
            testRunBuilder.notifications(notificationTargets);
            return testRunBuilder.build();
        }

    }

}
