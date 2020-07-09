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
                                       // temporarily returning back the launch context with id 1
                                       .launchContext(new TestRunDTO.LaunchContextDTO("1", "1"))
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
        startTest(uniqueId, ts, false);
    }

    @Override
    public void startHeadlessTest(String uniqueId, TestStartDescriptor ts) {
        startTest(uniqueId, ts, true);
    }

    private void startTest(String uniqueId, TestStartDescriptor ts, boolean headless) {
        TestRunDescriptor testRun = RunContext.getRun();

        TestDTO test = TestDTO.builder()
                              .uuid(ts.getUuid())
                              .name(ts.getName())
                              .className(ts.getTestClass().getName())
                              .methodName(ts.getTestMethod().getName())
                              .startedAt(ts.getStartedAt())
                              .build();

        if (!headless) {
            String maintainer = ts.getMaintainer();
            // if owner was not provided try to detect owner by picking annotation value
            maintainer = maintainer == null ? MaintainerProcessor.retrieveOwner(ts.getTestClass(), ts.getTestMethod()) : maintainer;

            test.setMaintainer(maintainer);

            TestDescriptor headlessTest = RunContext.getCurrentTest();
            if (headlessTest != null) {
                test.setId(Long.valueOf(headlessTest.getZebrunnerId()));
            }
        }

        test = API_CLIENT.registerTestStart(Long.valueOf(testRun.getZebrunnerId()), test, headless);

        TestDescriptor testDescriptor = TestDescriptor.create(String.valueOf(test.getId()), ts);

        RunContext.putTest(uniqueId, testDescriptor);
        SessionRegistrar.addTestRef(String.valueOf(test.getId()));
    }

    @Override
    public boolean isTestStarted(String uniqueId) {
        return RunContext.getTest(uniqueId) != null;
    }

    @Override
    public void finishTest(String uniqueId, TestFinishDescriptor tf) {
        TestRunDescriptor testRun = RunContext.getRun();
        TestDescriptor test = RunContext.getTest(uniqueId);

        String zebrunnerId = test.getZebrunnerId();
        TestDTO result = TestDTO.builder()
                                .id(Long.valueOf(zebrunnerId))
                                .result(tf.getStatus().name())
                                .reason(tf.getStatusReason())
                                .endedAt(tf.getEndedAt())
                                .build();

        API_CLIENT.registerTestFinish(Long.valueOf(testRun.getZebrunnerId()), result);

        RunContext.completeTest(uniqueId, tf);
        SessionRegistrar.addTestRef(zebrunnerId);
        SessionRegistrar.clearTestRef(zebrunnerId);
    }

    public static ReportingRegistrar getInstance() {
        return INSTANCE;
    }

    static ZebrunnerApiClient getApiClient() {
        return API_CLIENT;
    }
}
