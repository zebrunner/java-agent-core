package com.zebrunner.agent.core.listener;

import com.zebrunner.agent.core.client.domain.TestDTO;

import java.util.List;

public interface RerunListener extends AgentListener {

    void onRerun(List<TestDTO> tests);

}
