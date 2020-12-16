package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentTest {

    private static final ZebrunnerApiClient API_CLIENT = ZebrunnerApiClient.getInstance();

    public static void revertRegistration() {
        TestRunDescriptor run = RunContext.getRun();
        RunContext.removeCurrentTest()
                  .map(TestDescriptor::getZebrunnerId)
                  .ifPresent(currentTestId -> API_CLIENT.revertTestRegistration(run.getZebrunnerId(), currentTestId));
    }

}
