package com.zebrunner.agent.core.registrar.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutenticationData {

    private Integer userId;

    private String authTokenType;
    private String authToken;
    private int authTokenExpirationInSecs;

    private String refreshToken;

    private String tenantName;

}
