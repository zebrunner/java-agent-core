package com.zebrunner.agent.core.registrar.domain;

import lombok.Value;

import java.util.Map;

@Value
public class CiContext {

    CiType ciType;
    Map<String, String> envVariables;

}
