package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.annotation.TestCaseStatusOnFail;
import com.zebrunner.agent.core.annotation.TestCaseStatusOnPass;
import com.zebrunner.agent.core.annotation.TestCaseStatusOnSkip;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.domain.TcmType;
import com.zebrunner.agent.core.registrar.domain.TestCaseResult;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
class TestCasesRegistry {

    private static final class InstanceHolder {

        private static final TestCasesRegistry INSTANCE = new TestCasesRegistry();

    }

    static TestCasesRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final Map<TcmType, String> TCM_TYPE_TO_LABEL_KEY = new EnumMap<>(TcmType.class);

    static {
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.TEST_RAIL, "com.zebrunner.app/tcm.testrail.case-id");
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.XRAY, "com.zebrunner.app/tcm.xray.test-key");
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.ZEPHYR, "com.zebrunner.app/tcm.zephyr.test-case-key");
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.ZEBRUNNER, "com.zebrunner.app/tcm.zebrunner.test-case-key");
    }

    private final ZebrunnerApiClient zebrunnerApiClient = ZebrunnerApiClient.getInstance();
    private final Map<Long, Map<TcmType, Map<String, String>>> testIdToTcmTypeToTestCaseIdToStatus = new HashMap<>();

    void addTestCasesToCurrentTest(TcmType tcmType, Collection<String> testCaseIds) {
        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testId -> {
                      Map<String, String> testCaseIdToStatus = this.getTestCaseIdToStatus(testId, tcmType);
                      testCaseIds.stream()
                                 .filter(testCaseId -> !testCaseIdToStatus.containsKey(testCaseId))
                                 .forEach(testCaseId -> {
                                     Label.attachToTest(TCM_TYPE_TO_LABEL_KEY.get(tcmType), testCaseId);
                                     testCaseIdToStatus.put(testCaseId, null);
                                 });
                  });
    }

    void setCurrentTestTestCaseStatus(TcmType tcmType, String testCaseId, String status) {
        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testId -> {
                      this.getTestCaseIdToStatus(testId, tcmType)
                          .put(testCaseId, status);

                      Set<TestCaseResult> results = Collections.singleton(new TestCaseResult(tcmType, testCaseId, status));
                      zebrunnerApiClient.upsertTestCaseResults(RunContext.getZebrunnerRunId(), testId, results);
                  });
    }

    private Map<String, String> getTestCaseIdToStatus(Long testId, TcmType tcmType) {
        return testIdToTcmTypeToTestCaseIdToStatus.computeIfAbsent(testId, $ -> new HashMap<>())
                                                  .computeIfAbsent(tcmType, $ -> new HashMap<>());
    }

    void setExplicitStatusesOnCurrentTestPass() {
        RunContext.getCurrentTest()
                  .ifPresent(test -> {
                      Long testId = test.getZebrunnerId();
                      String passStatus = this.getOnPassStatus(test);
                      if (passStatus != null && !passStatus.isEmpty()) {
                          this.setCaseStatusesIfThereIsNoExplicit(testId, passStatus);
                      }
                      testIdToTcmTypeToTestCaseIdToStatus.remove(testId);
                  });
    }

    void setExplicitStatusesOnCurrentTestFail() {
        RunContext.getCurrentTest()
                  .ifPresent(test -> {
                      Long testId = test.getZebrunnerId();
                      String failStatus = this.getOnFailStatus(test);
                      if (failStatus != null && !failStatus.isEmpty()) {
                          this.setCaseStatusesIfThereIsNoExplicit(testId, failStatus);
                      }
                      testIdToTcmTypeToTestCaseIdToStatus.remove(testId);
                  });
    }

    void setExplicitStatusesOnCurrentTestSkip() {
        RunContext.getCurrentTest()
                  .ifPresent(test -> {
                      Long testId = test.getZebrunnerId();
                      String skipStatus = this.getOnSkipStatus(test);
                      if (skipStatus != null && !skipStatus.isEmpty()) {
                          this.setCaseStatusesIfThereIsNoExplicit(testId, skipStatus);
                      }
                      testIdToTcmTypeToTestCaseIdToStatus.remove(testId);
                  });
    }

    private void setCaseStatusesIfThereIsNoExplicit(Long testId, String status) {
        List<TestCaseResult> results = new ArrayList<>();
        testIdToTcmTypeToTestCaseIdToStatus.computeIfAbsent(testId, $ -> new HashMap<>())
                                           .forEach((tcmType, testCaseIdToStatus) ->
                                                   testCaseIdToStatus.forEach((testCaseId, explicitStatus) -> {
                                                       if (explicitStatus == null) {
                                                           results.add(new TestCaseResult(tcmType, testCaseId, status));
                                                       }
                                                   })
                                           );

        if (!results.isEmpty()) {
            zebrunnerApiClient.upsertTestCaseResults(RunContext.getZebrunnerRunId(), testId, results);
        }
    }

    private String getOnPassStatus(TestDescriptor testDescriptor) {
        return this.getStatus(testDescriptor, TestCaseStatusOnPass.class, TestCaseStatusOnPass::value, ConfigurationHolder::getTestCaseStatusOnPass);
    }

    private String getOnFailStatus(TestDescriptor testDescriptor) {
        return this.getStatus(testDescriptor, TestCaseStatusOnFail.class, TestCaseStatusOnFail::value, ConfigurationHolder::getTestCaseStatusOnFail);
    }

    private String getOnSkipStatus(TestDescriptor testDescriptor) {
        return this.getStatus(testDescriptor, TestCaseStatusOnSkip.class, TestCaseStatusOnSkip::value, ConfigurationHolder::getTestCaseStatusOnSkip);
    }

    private <T extends Annotation> String getStatus(TestDescriptor testDescriptor,
                                                    Class<T> annotationClass,
                                                    Function<T, String> statusValueExtractor,
                                                    Supplier<String> globalStatusExtractor) {
        return Optional.ofNullable(testDescriptor.getTestMethod())
                       .map(method -> method.getAnnotation(annotationClass))
                       .map(statusValueExtractor)
                       .orElseGet(() -> Optional.ofNullable(testDescriptor.getTestClass())
                                                .map(clazz -> clazz.getAnnotation(annotationClass))
                                                .map(statusValueExtractor)
                                                .orElseGet(globalStatusExtractor)
                       );
    }

}
