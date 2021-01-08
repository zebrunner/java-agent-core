package com.zebrunner.agent.core.config;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.exception.TestAgentException;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ConfigurationProviderChain implements ConfigurationProvider {

    private static final String DEFAULT_PROJECT = "UNKNOWN";

    private final List<ConfigurationProvider> providers = new LinkedList<>();

    public ConfigurationProviderChain(List<? extends ConfigurationProvider> credentialsProviders) {
        if (credentialsProviders == null || credentialsProviders.size() == 0) {
            throw new IllegalArgumentException("No credential providers specified");
        }
        this.providers.addAll(credentialsProviders);
    }

    @Override
    public ReportingConfiguration getConfiguration() {
        ReportingConfiguration config = ReportingConfiguration.builder()
                                                              .run(new ReportingConfiguration.RunConfiguration())
                                                              .server(new ReportingConfiguration.ServerConfiguration())
                                                              .rerun(new ReportingConfiguration.RerunConfiguration())
                                                              .build();
        assembleConfiguration(config);
        if (areMandatoryArgsSet(config)) {
            return config;
        } else {
            throw new TestAgentException("Mandatory agent properties are missing - double-check agent configuration");
        }
    }

    /**
     * Iterates over all configuration providers and assembles agent configuration. Configuration property
     * supplied by provider with highest priority always takes precedence.
     *
     * @param config configuration to be assembled
     */
    private void assembleConfiguration(ReportingConfiguration config) {
        for (ConfigurationProvider provider : providers) {
            try {
                ReportingConfiguration providedConfig = provider.getConfiguration();
                normalize(providedConfig);
                merge(config, providedConfig);
                // no need to iterate further to provider with lower priority if all args are provided already
                if (areAllArgsSet(providedConfig)) {
                    break;
                }
            } catch (TestAgentException e) {
                log.warn(e.getMessage());
            }
        }
        if (config.getProjectKey() == null || config.getProjectKey().isEmpty()) {
            config.setProjectKey(DEFAULT_PROJECT);
        }
    }

    private static void normalize(ReportingConfiguration config) {
        normalizeServerConfiguration(config);
        normalizeRunConfiguration(config);
        normalizeRerunConfiguration(config);
    }

    private static void normalizeServerConfiguration(ReportingConfiguration config) {
        if (config.getServer() == null) {
            config.setServer(new ReportingConfiguration.ServerConfiguration());
        } else {
            ReportingConfiguration.ServerConfiguration serverConfig = config.getServer();
            String hostname = serverConfig.getHostname();
            String accessToken = serverConfig.getHostname();
            if (hostname != null && accessToken.isEmpty()) {
                serverConfig.setHostname(null);
            }
            if (accessToken != null && accessToken.isEmpty()) {
                serverConfig.setAccessToken(null);
            }
        }
    }

    private static void normalizeRunConfiguration(ReportingConfiguration config) {
        if (config.getRun() == null) {
            config.setRerun(new ReportingConfiguration.RerunConfiguration());
        } else {
            ReportingConfiguration.RunConfiguration runConfig = config.getRun();
            String displayName = runConfig.getDisplayName();
            String build = runConfig.getBuild();
            String environment = runConfig.getEnvironment();
            if (displayName != null && displayName.isEmpty()) {
                runConfig.setDisplayName(null);
            }
            if (build != null && build.isEmpty()) {
                runConfig.setBuild(null);
            }
            if (environment != null && environment.isEmpty()) {
                runConfig.setEnvironment(null);
            }
        }
    }

    private static void normalizeRerunConfiguration(ReportingConfiguration config) {
        if (config.getRerun() == null) {
            config.setRerun(new ReportingConfiguration.RerunConfiguration());
        } else {
            ReportingConfiguration.RerunConfiguration rerunConfig = config.getRerun();
            String runId = rerunConfig.getRunId();
            if (runId != null && runId.isEmpty()) {
                rerunConfig.setRunId(null);
            }
        }
    }

    /**
     * Sets values coming from provided configuration that were not set previously by providers with higher priority
     *
     * @param config         configuration assembled by previous configuration providers
     * @param providedConfig configuration from current configuration provider
     */
    private static void merge(ReportingConfiguration config, ReportingConfiguration providedConfig) {
        if (config.getReportingEnabled() == null) {
            config.setReportingEnabled(providedConfig.getReportingEnabled());
        }

        if (config.getProjectKey() == null) {
            config.setProjectKey(providedConfig.getProjectKey());
        }

        ReportingConfiguration.ServerConfiguration server = config.getServer();
        if (server.getHostname() == null) {
            server.setHostname(providedConfig.getServer().getHostname());
        }
        if (server.getAccessToken() == null) {
            server.setAccessToken(providedConfig.getServer().getAccessToken());
        }

        ReportingConfiguration.RunConfiguration run = config.getRun();
        if (run.getDisplayName() == null) {
            run.setDisplayName(providedConfig.getRun().getDisplayName());
        }
        if (run.getBuild() == null) {
            run.setBuild(providedConfig.getRun().getBuild());
        }
        if (run.getEnvironment() == null) {
            run.setEnvironment(providedConfig.getRun().getEnvironment());
        }

        ReportingConfiguration.RerunConfiguration rerun = config.getRerun();
        if (rerun.getRunId() == null) {
            rerun.setRunId(providedConfig.getRerun().getRunId());
        }
    }

    // project-key is not considered as a mandatory property
    private static boolean areMandatoryArgsSet(ReportingConfiguration config) {
        ReportingConfiguration.ServerConfiguration server = config.getServer();

        // no need to check anything if reporting is disabled
        return !config.isReportingEnabled() || (server.getHostname() != null && server.getAccessToken() != null);
    }

    private static boolean areAllArgsSet(ReportingConfiguration config) {
        Boolean enabled = config.getReportingEnabled();
        String projectKey = config.getProjectKey();
        String hostname = config.getServer().getHostname();
        String accessToken = config.getServer().getAccessToken();
        String displayName = config.getRun().getDisplayName();
        String build = config.getRun().getBuild();
        String environment = config.getRun().getEnvironment();
        String runId = config.getRerun().getRunId();

        return enabled != null
                && projectKey != null
                && hostname != null && accessToken != null
                && displayName != null && build != null && environment != null
                && runId != null;
    }

}
