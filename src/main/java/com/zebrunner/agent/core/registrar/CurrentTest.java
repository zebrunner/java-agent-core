package com.zebrunner.agent.core.registrar;

import lombok.experimental.UtilityClass;

import java.util.Optional;

import com.zebrunner.agent.core.registrar.client.ApiClientRegistry;
import com.zebrunner.agent.core.registrar.client.ZebrunnerApiClient;
import com.zebrunner.agent.core.registrar.domain.Test;

@UtilityClass
public class CurrentTest {

    private static final ZebrunnerApiClient API_CLIENT = ApiClientRegistry.getClient();

    /**
     * This method returns Zebrunner Test id.
     * @return if test has not been reported yet - empty {@link Optional}, otherwise - {@link Optional} containing Zebrunner Test id.
     */
    public static Optional<Long> getId() {
        return ReportingContext.getCurrentTest()
                               .map(Test::getId);
    }

    public static void revertRegistration() {
        ReportingContext.getTestRunId()
                        .ifPresent(testRunId -> ReportingContext.removeCurrentTest()
                                                                .map(Test::getId)
                                                                .ifPresent(currentTestId -> API_CLIENT.revertTestRegistration(testRunId, currentTestId)));
    }

}
