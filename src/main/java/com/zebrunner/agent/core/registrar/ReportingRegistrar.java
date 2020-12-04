package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunDescriptor;
import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.core.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.client.domain.TestDTO;
import com.zebrunner.agent.core.client.domain.TestRunDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
class ReportingRegistrar implements TestRunRegistrar {

    static {
        RerunResolver.resolve();
    }

    private static final ReportingRegistrar INSTANCE = new ReportingRegistrar();
    private static final String CI_RUN_ID = System.getProperty("ci_run_id");

    public static ReportingRegistrar getInstance() {
        return INSTANCE;
    }

    private final ZebrunnerApiClient apiClient = ZebrunnerApiClient.getInstance();
    private final CompositeLabelResolver labelResolver = new CompositeLabelResolver();
    private final WebSessionRegistrar webSessionRegistrar = WebSessionRegistrar.getInstance();
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

        TestRunDescriptor testRunDescriptor = TestRunDescriptor.create(testRun.getId(), tr);
        RunContext.setRun(testRunDescriptor);
    }

    @Override
    public void registerFinish(TestRunFinishDescriptor finishDescriptor) {
        TestRunDTO testRun = TestRunDTO.builder()
                                       .id(RunContext.getRun().getZebrunnerId())
                                       .endedAt(finishDescriptor.getEndedAt())
                                       .build();
        apiClient.registerTestRunFinish(testRun);

        RunContext.getRun().complete(finishDescriptor);
    }

    @Override
    public void registerHeadlessTestStart(String id, TestStartDescriptor ts) {
        TestDTO test = TestDTO.builder()
                              .name(ts.getName())
                              .startedAt(ts.getStartedAt())
                              .build();

        test = apiClient.registerTestStart(RunContext.getRun().getZebrunnerId(), test, true);

        TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
        RunContext.addTest(id, testDescriptor);
        webSessionRegistrar.linkAllCurrentToTest(test.getId());
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
                              .build();

        TestDescriptor headlessTest = RunContext.getCurrentTest();
        if (headlessTest != null) {
            test.setId(headlessTest.getZebrunnerId());
            test = apiClient.registerHeadlessTestUpdate(RunContext.getRun().getZebrunnerId(), test);
        } else {
            test = apiClient.registerTestStart(RunContext.getRun().getZebrunnerId(), test, false);
        }

        TestDescriptor testDescriptor = TestDescriptor.create(test.getId(), ts);
        RunContext.addTest(id, testDescriptor);
        webSessionRegistrar.linkAllCurrentToTest(test.getId());
    }

    @Override
    public boolean isTestStarted(String id) {
        return RunContext.getTest(id) != null;
    }

    @Override
    public void registerTestFinish(String id, TestFinishDescriptor tf) {
        TestDescriptor testDescriptor = RunContext.getTest(id);

        TestStartDescriptor testStartDescriptor = testDescriptor.getStartDescriptor();
        Map<String, List<String>> labels = labelResolver.resolve(
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

        apiClient.registerTestFinish(RunContext.getRun().getZebrunnerId(), result);

        RunContext.completeTest(id, tf);
    }

}
