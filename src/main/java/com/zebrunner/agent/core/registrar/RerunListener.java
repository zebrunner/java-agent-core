package com.zebrunner.agent.core.registrar;

import java.util.List;

import com.zebrunner.agent.core.registrar.domain.RunContextTestSummary;

public interface RerunListener {

    void onRerun(List<RunContextTestSummary> testsToRerun);

}
