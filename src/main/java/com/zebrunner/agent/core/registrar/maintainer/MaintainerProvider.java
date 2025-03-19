package com.zebrunner.agent.core.registrar.maintainer;

import java.util.List;

import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.domain.TestRun;
import com.zebrunner.agent.core.registrar.domain.TestSession;

public interface MaintainerProvider {

    String provide(TestRun testRun, TestStart testStart, List<TestSession> testSessions);

}
