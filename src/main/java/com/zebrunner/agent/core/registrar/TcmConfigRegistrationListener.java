package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.client.request.UpdateLaunchTcmConfigRequest;
import com.zebrunner.agent.core.registrar.domain.TestRunStart;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TcmConfigRegistrationListener implements RegistrationListener {

    @Getter
    private static final TcmConfigRegistrationListener instance = new TcmConfigRegistrationListener();

    private final ZebrunnerApiClient apiClient = ApiClientRegistry.getClient();

    @Override
    public void onAfterTestRunStart(TestRunStart testRunStart) {
        ReportingConfiguration.Tcm.TestCaseStatus status = ConfigurationHolder.getTestCaseStatus();

        if (status.hasAnySpecified()) {
            UpdateLaunchTcmConfigRequest request = new UpdateLaunchTcmConfigRequest().setStatusOnPass(status.getOnPass())
                                                                                     .setStatusOnFail(status.getOnFail())
                                                                                     .setStatusOnKnownIssue(status.getOnKnownIssue())
                                                                                     .setStatusOnSkip(status.getOnSkip())
                                                                                     .setStatusOnBlock(status.getOnBlock());

            apiClient.patchTestRunTcmConfig(ReportingContext.getNullableTestRunId(), request);
        }
    }

}
