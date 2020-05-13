package com.zebrunner.agent.core.rest.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthTokenDTO {

    private String type;
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private String tenant;

}
