package com.zebrunner.agent.core.registrar.ci;

import com.zebrunner.agent.core.registrar.domain.CiContext;

public interface CiContextResolver {

    static CiContextResolver getInstance() {
        return CompositeCiContextResolver.getInstance();
    }

    CiContext resolve();

}
