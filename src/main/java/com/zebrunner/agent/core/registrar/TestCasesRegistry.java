package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.zebrunner.agent.core.annotation.TestCaseStatusOnBlock;
import com.zebrunner.agent.core.annotation.TestCaseStatusOnFail;
import com.zebrunner.agent.core.annotation.TestCaseStatusOnPass;
import com.zebrunner.agent.core.annotation.TestCaseStatusOnSkip;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.domain.TcmType;
import com.zebrunner.agent.core.registrar.domain.TestCaseResult;

@Slf4j
class TestCasesRegistry {

    private static final class InstanceHolder {

        private static final TestCasesRegistry INSTANCE = new TestCasesRegistry();

    }

    static TestCasesRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }

    // used when no status is set, since "" is a status that can be explicitly set.
    // push to tcm is disabled for test cases with "" status
    private static final String UNSET_STATUS = "<unset>";
    private static final Map<TcmType, String> TCM_TYPE_TO_LABEL_KEY = new EnumMap<>(TcmType.class);

    static {
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.TEST_RAIL, "com.zebrunner.app/tcm.testrail.case-id");
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.XRAY, "com.zebrunner.app/tcm.xray.test-key");
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.ZEPHYR, "com.zebrunner.app/tcm.zephyr.test-case-key");
        TCM_TYPE_TO_LABEL_KEY.put(TcmType.ZEBRUNNER, "com.zebrunner.app/tcm.zebrunner.test-case-key");
    }

    private final ZebrunnerApiClient zebrunnerApiClient = ClientRegistrar.getClient();
    private final Map<Long, Map<TcmType, Map<String, String>>> testIdToTcmTypeToTestCaseIdToStatus = new ConcurrentHashMap<>();

    void addTestCasesToCurrentTest(TcmType tcmType, Collection<String> testCaseIds) {
        RunContext.getCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(testId -> {
                      Map<String, String> testCaseIdToStatus = this.getTestCaseIdToStatus(testId, tcmType);
                      testCaseIds.stream()
                                 .filter(testCaseId -> !testCaseIdToStatus.containsKey(testCaseId))
                                 .forEach(testCaseId -> {
                                     Label.attachToTest(TCM_TYPE_TO_LABEL_KEY.get(tcmType), testCaseId);
                                     testCaseIdToStatus.put(testCaseId, UNSET_STATUS);
                                 });
                  });
    }

    void setCurrentTestTestCaseStatus(TcmType tcmType, String testCaseId, String status) {
        Objects.requireNonNull(status, "Status cannot be null.");

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
        return testIdToTcmTypeToTestCaseIdToStatus.computeIfAbsent(testId, $ -> new ConcurrentHashMap<>())
                                                  .computeIfAbsent(tcmType, $ -> new ConcurrentHashMap<>());
    }

    void setExplicitStatusesOnCurrentTestPass() {
        RunContext.getCurrentTest()
                  .ifPresent(test -> {
                      Long testId = test.getZebrunnerId();
                      String passStatus = this.getOnPassStatus(test);
                      if (passStatus != null) {
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
                      if (failStatus != null) {
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
                      if (skipStatus != null) {
                          this.setCaseStatusesIfThereIsNoExplicit(testId, skipStatus);
                      }
                      testIdToTcmTypeToTestCaseIdToStatus.remove(testId);
                  });
    }

    void setExplicitStatusesOnCurrentTestBlock() {
        RunContext.getCurrentTest()
                  .ifPresent(test -> {
                      Long testId = test.getZebrunnerId();
                      String blockStatus = this.getOnBlockStatus(test);
                      if (blockStatus != null) {
                          this.setCaseStatusesIfThereIsNoExplicit(testId, blockStatus);
                      }
                      testIdToTcmTypeToTestCaseIdToStatus.remove(testId);
                  });
    }

    private void setCaseStatusesIfThereIsNoExplicit(Long testId, String status) {
        List<TestCaseResult> results = new ArrayList<>();
        testIdToTcmTypeToTestCaseIdToStatus.computeIfAbsent(testId, $ -> new ConcurrentHashMap<>())
                                           .forEach((tcmType, testCaseIdToStatus) ->
                                                   testCaseIdToStatus.forEach((testCaseId, explicitStatus) -> {
                                                       if (UNSET_STATUS.equals(explicitStatus)) {
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

    private String getOnBlockStatus(TestDescriptor testDescriptor) {
        return this.getStatus(testDescriptor, TestCaseStatusOnBlock.class, TestCaseStatusOnBlock::value, ConfigurationHolder::getTestCaseStatusOnBlock);
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
