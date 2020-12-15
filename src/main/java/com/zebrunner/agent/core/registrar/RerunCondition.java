package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class RerunCondition {

    private final String runId;
    private final Set<Long> testIds;
    private final Set<Status> statuses;

}
