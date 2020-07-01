package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.reporting.MaintainerProcessor;
import com.zebrunner.agent.core.rest.ZebrunnerApiClient;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import com.zebrunner.agent.core.rest.domain.TestRunDTO;

class ReportingRegistrar implements TestRunRegistrar {

    private static final ReportingRegistrar INSTANCE = new ReportingRegistrar();
    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    static {
        RerunResolver.resolve();
    }

    @Override
    public void start(TestRunStartDescriptor tr) {
        TestRunDTO testRun = TestRunDTO.builder()
                                       .uuid(RerunResolver.getRunId())
                                       .name(tr.getName())
                                       .framework(tr.getFramework())
                                       .startedAt(tr.getStartedAt())
                                       .build();

        testRun = API_CLIENT.registerTestRunStart(testRun);

        TestRunDescriptor testRunDescriptor = TestRunDescriptor.create(String.valueOf(testRun.getId()), tr);
        RunContext.putRun(testRunDescriptor);
    }

    @Override
    public void finish(TestRunFinishDescriptor finishDescriptor) {
        TestRunDescriptor testRunDescriptor = RunContext.getRun();

        TestRunDTO testRun = TestRunDTO.builder()
                                       .id(Long.valueOf(testRunDescriptor.getZebrunnerId()))
                                       .endedAt(finishDescriptor.getEndedAt())
                                       .build();
        API_CLIENT.registerTestRunFinish(testRun);

        RunContext.getRun().complete(finishDescriptor);
    }

    @Override
    public void startTest(String uniqueId, TestStartDescriptor ts) {
        TestRunDescriptor testRun = RunContext.getRun();

        String owner = ts.getMaintainer();
        // if owner was not provided try to detect owner by picking annotation value
        owner = owner == null ? MaintainerProcessor.retrieveOwner(ts.getTestClass(), ts.getTestMethod()) : owner;

        TestDTO test = TestDTO.builder()
                              .name(ts.getName())
                              .className(ts.getTestClass().getName())
                              .methodName(ts.getTestMethod().getName())
                              .maintainer(owner)
                              .uuid(ts.getUuid())
                              .startedAt(ts.getStartedAt())
                              .build();

        test = API_CLIENT.registerTestStart(Long.valueOf(testRun.getZebrunnerId()), test);

        TestDescriptor testDescriptor = TestDescriptor.create(String.valueOf(test.getId()), ts);
        RunContext.putTest(uniqueId, testDescriptor);
    }

    @Override
    public boolean isTestStarted(String uniqueId) {
        return RunContext.getTest(uniqueId) != null;
    }

    @Override
    public void finishTest(String uniqueId, TestFinishDescriptor tf) {
        TestRunDescriptor testRun = RunContext.getRun();
        TestDescriptor test = RunContext.getTest(uniqueId);

        TestDTO result = TestDTO.builder()
                                .id(Long.valueOf(test.getZebrunnerId()))
                                .result(tf.getStatus().name())
                                .reason(tf.getStatusReason())
                                .endedAt(tf.getEndedAt())
                                .build();

        API_CLIENT.registerTestFinish(Long.valueOf(testRun.getZebrunnerId()), result);

        RunContext.completeTest(uniqueId, tf);
    }

    public static ReportingRegistrar getInstance() {
        return INSTANCE;
    }

    static ZebrunnerApiClient getApiClient() {
        return API_CLIENT;
    }
}
