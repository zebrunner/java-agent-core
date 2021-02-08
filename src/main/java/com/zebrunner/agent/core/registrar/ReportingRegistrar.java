package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import com.zebrunner.agent.core.registrar.label.LabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;
import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import com.zebrunner.agent.core.rest.domain.TestRunDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReportingRegistrar implements TestRunRegistrar {

    private static final ReportingRegistrar INSTANCE = new ReportingRegistrar();
    private static final LabelResolver LABEL_RESOLVER = new CompositeLabelResolver();
    private static final MaintainerResolver MAINTAINER_RESOLVER = new ChainedMaintainerResolver();
    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    static {
        RerunResolver.resolve();
    }

    @Override
    public void start(TestRunStartDescriptor tr) {
        TestRunDTO testRun = TestRunBuilder.build(tr);
        testRun = API_CLIENT.registerTestRunStart(testRun);

        TestRunDescriptor testRunDescriptor = TestRunDescriptor.create(testRun.getId(), tr);
        RunContext.putRun(testRunDescriptor);
    }

    @Override
    public void finish(TestRunFinishDescriptor finishDescriptor) {
        TestRunDescriptor testRunDescriptor = RunContext.getRun();

        TestRunDTO testRun = TestRunDTO.builder()
                                       .id(testRunDescriptor.getZebrunnerId())
                                       .endedAt(finishDescriptor.getEndedAt())
                                       .build();
        API_CLIENT.registerTestRunFinish(testRun);

        RunContext.getRun().complete(finishDescriptor);
    }

    @Override
    public void startHeadlessTest(String uniqueId, TestStartDescriptor ts) {
        TestRunDescriptor testRun = RunContext.getRun();

        TestDTO test = TestDTO.builder()
                              .name(ts.getName())
                              .startedAt(ts.getStartedAt())
                              .build();

        test = API_CLIENT.registerTestStart(testRun.getZebrunnerId(), test, true);

        TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
        RunContext.putTest(uniqueId, testDescriptor);
    }

    @Override
    public void startTest(String uniqueId, TestStartDescriptor ts) {
        TestRunDescriptor testRun = RunContext.getRun();

        TestDTO test = TestDTO.builder()
                              .uuid(ts.getUuid())
                              .name(ts.getName())
                              .className(ts.getTestClass().getName())
                              .methodName(ts.getTestMethod().getName())
                              .maintainer(MAINTAINER_RESOLVER.resolve(ts.getTestClass(), ts.getTestMethod()))
                              .startedAt(ts.getStartedAt())
                              .build();

        TestDescriptor headlessTest = RunContext.getCurrentTest();
        if (headlessTest != null) {
            test.setId(headlessTest.getZebrunnerId());
            test = API_CLIENT.registerHeadlessTestUpdate(testRun.getZebrunnerId(), test);
        } else {
            test = API_CLIENT.registerTestStart(testRun.getZebrunnerId(), test, false);
        }

        TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
        RunContext.putTest(uniqueId, testDescriptor);
    }

    @Override
    public boolean isTestStarted(String uniqueId) {
        return RunContext.getTest(uniqueId) != null;
    }

    @Override
    public void finishTest(String uniqueId, TestFinishDescriptor tf) {
        TestRunDescriptor testRun = RunContext.getRun();
        TestDescriptor testDescriptor = RunContext.getTest(uniqueId);

        TestStartDescriptor testStartDescriptor = testDescriptor.getStartDescriptor();
        Map<String, List<String>> labels = LABEL_RESOLVER.resolve(
                testStartDescriptor.getTestClass(), testStartDescriptor.getTestMethod()
        );

        Long zebrunnerId = testDescriptor.getZebrunnerId();
        TestDTO result = TestDTO.builder()
                                .id(zebrunnerId)
                                .result(tf.getStatus().name())
                                .reason(tf.getStatusReason())
                                .endedAt(tf.getEndedAt())
                                .labels(labels)
                                .artifactReferences(ArtifactReference.popAll())
                                .build();

        API_CLIENT.registerTestFinish(testRun.getZebrunnerId(), result);

        RunContext.completeTest(uniqueId, tf);
    }

    public static ReportingRegistrar getInstance() {
        return INSTANCE;
    }

    static ZebrunnerApiClient getApiClient() {
        return API_CLIENT;
    }

    private static final class TestRunBuilder {

        private static final int SLACK_CHANNELS_LIMIT = 20;
        private static final int MICROSOFT_TEAMS_CHANNELS_LIMIT = 20;

        private static final String SLACK_CHANNELS_NOTIFICATION_TYPE = "SLACK_CHANNELS";
        private static final String MICROSOFT_TEAMS_CHANNELS_NOTIFICATION_TYPE = "MS_TEAMS_CHANNELS";

        public static TestRunDTO build(TestRunStartDescriptor testRunStartDescriptor) {
            TestRunDTO.TestRunDTOBuilder testRunBuilder = TestRunDTO.builder()
                                                                    .uuid(RerunResolver.getRunId())
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

            Set<TestRunDTO.NotificationDTO> msTeamsChannels = ConfigurationHolder.getMicrosoftTeamsChannels()
                                                                                 .stream()
                                                                                 .limit(MICROSOFT_TEAMS_CHANNELS_LIMIT)
                                                                                 .map(channel -> new TestRunDTO.NotificationDTO(MICROSOFT_TEAMS_CHANNELS_NOTIFICATION_TYPE, channel))
                                                                                 .collect(Collectors.toSet());

            Set<TestRunDTO.NotificationDTO> notificationTargets = Stream.of(slackChannels, msTeamsChannels)
                                                                        .flatMap(Set::stream)
                                                                        .collect(Collectors.toSet());
            testRunBuilder.notifications(notificationTargets);
            return testRunBuilder.build();
        }

    }

}
