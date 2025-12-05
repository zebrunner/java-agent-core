package com.zebrunner.agent.core.registrar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.registrar.domain.TestRunStart;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LocaleRegistrationListener implements RegistrationListener {

    @Getter
    private static final LocaleRegistrationListener instance = new LocaleRegistrationListener();

    @Override
    public void onAfterTestRunStart(TestRunStart testRunStart) {
        ReportingConfiguration configuration = ConfigurationHolder.get();
        String locale = configuration.getRun().getLocale();

        if (locale != null) {
            CurrentTestRun.setLocale(locale);
        }
    }

}
