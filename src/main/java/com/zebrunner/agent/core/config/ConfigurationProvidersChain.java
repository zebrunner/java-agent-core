package com.zebrunner.agent.core.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.zebrunner.agent.core.config.provider.EnvironmentConfigurationProvider;
import com.zebrunner.agent.core.config.provider.PropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.SystemPropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.YamlConfigurationProvider;
import com.zebrunner.agent.core.exception.TestAgentException;

@Slf4j
@Getter(AccessLevel.PACKAGE)
class ConfigurationProvidersChain {

    private static final String DEFAULT_PROJECT = "DEF";
    private static final ConfigurationProvidersChain INSTANCE = new ConfigurationProvidersChain();

    public static ConfigurationProvidersChain getInstance() {
        return INSTANCE;
    }

    private final List<ConfigurationProvider> configurationProviders;

    private ConfigurationProvidersChain() {
        configurationProviders = new ArrayList<>(Arrays.asList(
                new EnvironmentConfigurationProvider(),
                new SystemPropertiesConfigurationProvider(),
                new YamlConfigurationProvider(),
                new PropertiesConfigurationProvider()
        ));
    }

    public ReportingConfiguration getConfiguration() {
        ReportingConfiguration config = new ReportingConfiguration();
        this.assembleConfiguration(config);

        // project-key is not considered as a mandatory property
        if (!config.isReportingEnabled() || config.getServer().areAllSet()) {
            return config;
        }

        throw new TestAgentException("Mandatory agent properties are missing - double-check agent configuration");
    }

    /**
     * Iterates over all configuration providers and assembles agent configuration. Configuration property
     * supplied by provider with the highest priority always takes precedence.
     *
     * @param config configuration to be assembled
     */
    private void assembleConfiguration(ReportingConfiguration config) {
        for (ConfigurationProvider provider : configurationProviders) {
            try {
                ReportingConfiguration providedConfig = provider.getConfiguration();

                this.normalizeServerConfiguration(providedConfig);
                this.normalizeRunConfiguration(providedConfig);
                this.normalizeMilestoneConfiguration(providedConfig);
                this.normalizeNotificationConfiguration(providedConfig);
                this.normalizeTcmConfiguration(providedConfig);

                config.copyMissing(providedConfig);
            } catch (TestAgentException e) {
                log.warn("Could not handle configuration from provider {}", provider.getClass().getName(), e);
            }
        }

        if (config.getProjectKey() == null || config.getProjectKey().trim().isEmpty()) {
            config.setProjectKey(DEFAULT_PROJECT);
        }
    }

    private void normalizeServerConfiguration(ReportingConfiguration config) {
        ReportingConfiguration.ServerConfiguration server = config.getServer();

        this.setNullIfBlank(server::getHostname, server::setHostname);
        this.setNullIfBlank(server::getAccessToken, server::setAccessToken);
    }

    private void normalizeRunConfiguration(ReportingConfiguration config) {
        ReportingConfiguration.RunConfiguration run = config.getRun();

        this.setNullIfBlank(run::getDisplayName, run::setDisplayName);
        this.setNullIfBlank(run::getBuild, run::setBuild);
        this.setNullIfBlank(run::getEnvironment, run::setEnvironment);
        this.setNullIfBlank(run::getContext, run::setContext);
    }

    private void normalizeMilestoneConfiguration(ReportingConfiguration config) {
        ReportingConfiguration.Milestone milestone = config.getMilestone();

        this.setNullIfBlank(milestone::getName, milestone::setName);
    }

    private void normalizeNotificationConfiguration(ReportingConfiguration config) {
        ReportingConfiguration.Notification notification = config.getNotification();

        this.setNullIfBlank(notification::getSlackChannels, notification::setSlackChannels);
        this.setNullIfBlank(notification::getMsTeamsChannels, notification::setMsTeamsChannels);
        this.setNullIfBlank(notification::getEmails, notification::setEmails);
    }

    private void normalizeTcmConfiguration(ReportingConfiguration config) {
        ReportingConfiguration.Tcm tcmConfig = config.getTcm();

        this.normalizeTcmTestCaseStatus(tcmConfig);
        this.normalizeZebrunnerTcmConfiguration(tcmConfig);
        this.normalizeTestRailConfiguration(tcmConfig);
        this.normalizeXrayConfiguration(tcmConfig);
        this.normalizeZephyrConfiguration(tcmConfig);
    }

    private void normalizeTcmTestCaseStatus(ReportingConfiguration.Tcm tcm) {
        ReportingConfiguration.Tcm.TestCaseStatus testCaseStatus = tcm.getTestCaseStatus();

        String onPass = testCaseStatus.getOnPass();
        String onFail = testCaseStatus.getOnFail();
        String onKnownIssue = testCaseStatus.getOnKnownIssue();
        String onSkip = testCaseStatus.getOnSkip();
        String onBlock = testCaseStatus.getOnBlock();

        if (onPass != null) {
            testCaseStatus.setOnPass(onPass.trim());
        }
        if (onFail != null) {
            testCaseStatus.setOnFail(onFail.trim());
        }
        if (onKnownIssue != null) {
            testCaseStatus.setOnKnownIssue(onKnownIssue.trim());
        }
        if (onSkip != null) {
            testCaseStatus.setOnSkip(onSkip.trim());
        }
        if (onBlock != null) {
            testCaseStatus.setOnBlock(onBlock.trim());
        }
    }

    private void normalizeZebrunnerTcmConfiguration(ReportingConfiguration.Tcm tcm) {
        ReportingConfiguration.Tcm.Zebrunner zebrunner = tcm.getZebrunner();

        this.setNullIfBlank(zebrunner::getTestRunId, zebrunner::setTestRunId);
    }

    private void normalizeTestRailConfiguration(ReportingConfiguration.Tcm tcm) {
        ReportingConfiguration.Tcm.TestRail testRail = tcm.getTestRail();

        this.setNullIfBlank(testRail::getSuiteId, testRail::setSuiteId);
        this.setNullIfBlank(testRail::getRunId, testRail::setRunId);
        this.setNullIfBlank(testRail::getRunName, testRail::setRunName);
        this.setNullIfBlank(testRail::getMilestoneName, testRail::setMilestoneName);
        this.setNullIfBlank(testRail::getAssignee, testRail::setAssignee);
    }

    private void normalizeXrayConfiguration(ReportingConfiguration.Tcm tcm) {
        ReportingConfiguration.Tcm.Xray xray = tcm.getXray();

        this.setNullIfBlank(xray::getExecutionKey, xray::setExecutionKey);
    }

    private void normalizeZephyrConfiguration(ReportingConfiguration.Tcm tcm) {
        ReportingConfiguration.Tcm.Zephyr zephyr = tcm.getZephyr();

        this.setNullIfBlank(zephyr::getTestCycleKey, zephyr::setTestCycleKey);
        this.setNullIfBlank(zephyr::getJiraProjectKey, zephyr::setJiraProjectKey);
    }

    private void setNullIfBlank(Supplier<String> getter, Consumer<String> setter) {
        String value = getter.get();

        if (value != null && value.trim().isEmpty()) {
            setter.accept(null);
        }
    }

}
