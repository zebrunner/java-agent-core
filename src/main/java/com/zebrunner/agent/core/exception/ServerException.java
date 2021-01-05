package com.zebrunner.agent.core.exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class ServerException extends RuntimeException {

    String message;

}
