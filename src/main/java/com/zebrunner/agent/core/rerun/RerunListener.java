package com.zebrunner.agent.core.rerun;

import com.zebrunner.agent.core.rest.domain.TestDTO;

import java.util.List;

public interface RerunListener {

    void onRerun(List<TestDTO> tests);

}
