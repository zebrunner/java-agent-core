package com.zebrunner.agent.core.registrar;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.domain.TcmType;
import com.zebrunner.agent.core.registrar.domain.Test;
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

    private final ZebrunnerApiClient zebrunnerApiClient = ApiClientRegistry.getClient();
    private final Map<Long, Map<TcmType, Map<String, String>>> testIdToTcmTypeToTestCaseIdToStatus = new ConcurrentHashMap<>();

    void addTestCasesToCurrentTest(TcmType tcmType, Collection<String> testCaseIds) {
        Long testRunId = ReportingContext.getNullableTestRunId();
        if (testRunId == null) {
            return;
        }

        ReportingContext.getCurrentTest()
                        .map(Test::getId)
                        .ifPresent(testId -> {
                            List<TestCaseResult> newTestCases = new ArrayList<>();
                            Map<String, String> testCaseIdToStatus = this.getTestCaseIdToStatus(testId, tcmType);

                            for (String testCaseId : testCaseIds) {
                                String previousValue = testCaseIdToStatus.putIfAbsent(testCaseId, UNSET_STATUS);

                                if (previousValue == null) {
                                    newTestCases.add(new TestCaseResult(tcmType, testCaseId, null));
                                }
                            }

                            zebrunnerApiClient.upsertTestCaseResults(testRunId, testId, newTestCases);
                        });
    }

    void setCurrentTestTestCaseStatus(TcmType tcmType, String testCaseId, String status) {
        Long testRunId = ReportingContext.getNullableTestRunId();
        if (testRunId == null) {
            return;
        }

        Objects.requireNonNull(status, "Status cannot be null");

        ReportingContext.getCurrentTest()
                        .map(Test::getId)
                        .ifPresent(testId -> {
                            this.getTestCaseIdToStatus(testId, tcmType)
                                .put(testCaseId, status);

                            Set<TestCaseResult> results = Collections.singleton(new TestCaseResult(tcmType, testCaseId, status));
                            zebrunnerApiClient.upsertTestCaseResults(testRunId, testId, results);
                        });
    }

    private Map<String, String> getTestCaseIdToStatus(Long testId, TcmType tcmType) {
        return testIdToTcmTypeToTestCaseIdToStatus.computeIfAbsent(testId, $ -> new ConcurrentHashMap<>())
                                                  .computeIfAbsent(tcmType, $ -> new ConcurrentHashMap<>());
    }

    void setExplicitStatusesOnCurrentTestPass() {
        ReportingContext.getCurrentTest()
                        .ifPresent(test -> {
                            this.setCaseStatusesIfThereIsNoExplicit(test.getId(), this.getOnPassStatus(test));
                            testIdToTcmTypeToTestCaseIdToStatus.remove(test.getId());
                        });
    }

    void setExplicitStatusesOnCurrentTestFail() {
        ReportingContext.getCurrentTest()
                        .ifPresent(test -> {
                            this.setCaseStatusesIfThereIsNoExplicit(test.getId(), this.getOnFailStatus(test));
                            testIdToTcmTypeToTestCaseIdToStatus.remove(test.getId());
                        });
    }

    void setExplicitStatusesOnCurrentTestSkip() {
        ReportingContext.getCurrentTest()
                        .ifPresent(test -> {
                            this.setCaseStatusesIfThereIsNoExplicit(test.getId(), this.getOnSkipStatus(test));
                            testIdToTcmTypeToTestCaseIdToStatus.remove(test.getId());
                        });
    }

    void setExplicitStatusesOnCurrentTestBlock() {
        ReportingContext.getCurrentTest()
                        .ifPresent(test -> {
                            this.setCaseStatusesIfThereIsNoExplicit(test.getId(), this.getOnBlockStatus(test));
                            testIdToTcmTypeToTestCaseIdToStatus.remove(test.getId());
                        });
    }

    private void setCaseStatusesIfThereIsNoExplicit(Long testId, String status) {
        Long testRunId = ReportingContext.getNullableTestRunId();
        if (status == null || testRunId == null) {
            return;
        }

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
            zebrunnerApiClient.upsertTestCaseResults(testRunId, testId, results);
        }
    }

    private String getOnPassStatus(Test test) {
        return this.getStatus(test, TestCaseStatusOnPass.class, TestCaseStatusOnPass::value, ConfigurationHolder::getTestCaseStatusOnPass);
    }

    private String getOnFailStatus(Test test) {
        return this.getStatus(test, TestCaseStatusOnFail.class, TestCaseStatusOnFail::value, ConfigurationHolder::getTestCaseStatusOnFail);
    }

    private String getOnSkipStatus(Test test) {
        return this.getStatus(test, TestCaseStatusOnSkip.class, TestCaseStatusOnSkip::value, ConfigurationHolder::getTestCaseStatusOnSkip);
    }

    private String getOnBlockStatus(Test test) {
        return this.getStatus(test, TestCaseStatusOnBlock.class, TestCaseStatusOnBlock::value, ConfigurationHolder::getTestCaseStatusOnBlock);
    }

    private <T extends Annotation> String getStatus(Test test,
                                                    Class<T> annotationClass,
                                                    Function<T, String> statusValueExtractor,
                                                    Supplier<String> globalStatusExtractor) {
        return Optional.ofNullable(test.getTestMethod())
                       .map(method -> method.getAnnotation(annotationClass))
                       .map(statusValueExtractor)
                       .orElseGet(() -> Optional.ofNullable(test.getTestClass())
                                                .map(clazz -> clazz.getAnnotation(annotationClass))
                                                .map(statusValueExtractor)
                                                .orElseGet(globalStatusExtractor));
    }

}
