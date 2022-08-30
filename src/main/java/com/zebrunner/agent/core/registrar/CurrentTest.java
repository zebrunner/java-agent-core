package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentTest {

    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

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
