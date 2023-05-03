package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentTest {

    private static final ZebrunnerApiClient API_CLIENT = ClientRegistrar.getClient();

    /**
     * This method returns Zebrunner Test id.
     * @return if test has not been reported yet - empty {@link Optional}, otherwise - {@link Optional} containing Zebrunner Test id.
     */
    public static Optional<Long> getId() {
        return RunContext.getCurrentTest()
                         .map(TestDescriptor::getZebrunnerId);
    }

    public static void revertRegistration() {
        Long runId = RunContext.getZebrunnerRunId();

        RunContext.removeCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(currentTestId -> API_CLIENT.revertTestRegistration(runId, currentTestId));
    }

}
