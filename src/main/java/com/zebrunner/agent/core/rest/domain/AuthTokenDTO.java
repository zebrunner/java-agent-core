package com.zebrunner.agent.core.rest.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class AuthTokenDTO {

    private Integer userId;

    private String authTokenType;
    private String authToken;
    private int authTokenExpirationInSecs;

    private String refreshToken;

    private String tenantName;

}
