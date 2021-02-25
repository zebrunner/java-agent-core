package com.zebrunner.agent.core.registrar.domain;

import lombok.Value;

import java.util.Map;

@Value
public class CiContextDTO {

    CiType ciType;
    Map<String, String> envVariables;

}
