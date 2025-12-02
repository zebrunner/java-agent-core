package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.domain.TestRunStart;

@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TestRunTcmConfigurationRegistrationListener implements RegistrationListener {

    @Getter
    private static final TestRunTcmConfigurationRegistrationListener instance = new TestRunTcmConfigurationRegistrationListener();

    @Override
    public void onAfterTestRunStart(TestRunStart startDescriptor) {
        this.resolveZebrunnerConfiguration();
        this.resolveTestRailConfiguration();
        this.resolveXrayConfiguration();
        this.resolveZephyrConfiguration();
    }

    private void resolveZebrunnerConfiguration() {
        if (!ConfigurationHolder.isTcmSyncEnabled()) {
            TestCase.disableSync();
        }

        this.setIfNotNull(ConfigurationHolder.getTcmTestRunId(), TestCase::setTestRunId);

        if (ConfigurationHolder.isTcmRealTimeSyncEnabled()) {
            TestCase.enableRealTimeSync();
        }
    }

    private void resolveTestRailConfiguration() {
        if (!ConfigurationHolder.isTestRailSyncEnabled()) {
            TestRail.disableSync();
        }

        this.setIfNotNull(ConfigurationHolder.getTestRailSuiteId(), TestRail::setSuiteId);
        this.setIfNotNull(ConfigurationHolder.getTestRailRunId(), TestRail::setRunId);
        this.setIfNotNull(ConfigurationHolder.getTestRailRunName(), TestRail::setRunName);
        this.setIfNotNull(ConfigurationHolder.getTestRailMilestoneName(), TestRail::setMilestone);
        this.setIfNotNull(ConfigurationHolder.getTestRailAssignee(), TestRail::setAssignee);

        if (ConfigurationHolder.shouldTestRailIncludeAllTestCasesInNewRun()) {
            TestRail.includeAllTestCasesInNewRun();
        }
        if (ConfigurationHolder.isTestRailRealTimeSyncEnabled()) {
            TestRail.enableRealTimeSync();
        }
    }

    private void resolveXrayConfiguration() {
        if (!ConfigurationHolder.isXraySyncEnabled()) {
            Xray.disableSync();
        }

        this.setIfNotNull(ConfigurationHolder.getXrayExecutionKey(), Xray::setExecutionKey);

        if (ConfigurationHolder.isXrayRealTimeSyncEnabled()) {
            Xray.enableRealTimeSync();
        }
    }

    private void resolveZephyrConfiguration() {
        if (!ConfigurationHolder.isZephyrSyncEnabled()) {
            Zephyr.disableSync();
        }

        this.setIfNotNull(ConfigurationHolder.getZephyrTestCycleKey(), Zephyr::setTestCycleKey);
        this.setIfNotNull(ConfigurationHolder.getZephyrJiraProjectKey(), Zephyr::setJiraProjectKey);

        if (ConfigurationHolder.isZephyrSyncRealTimeEnabled()) {
            Zephyr.enableRealTimeSync();
        }
    }

    private <T> void setIfNotNull(T object, Consumer<T> setter) {
        if (object != null) {
            setter.accept(object);
        }
    }

}
