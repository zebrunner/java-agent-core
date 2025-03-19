package com.zebrunner.agent.core.registrar.domain;

import lombok.Data;

@Data
public class AuthenticationData {

    private String authTokenType;
    private String authToken;

}
