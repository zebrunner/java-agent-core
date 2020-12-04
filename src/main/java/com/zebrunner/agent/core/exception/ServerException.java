package com.zebrunner.agent.core.exception;

public class ServerException extends RuntimeException {

    private static final String SERVER_ERROR_MSG_FORMAT = "Server responded with status code %d %s. " +
            "Make sure that hostname is correct and auth token is valid. The desired project must also exist in Zebrunner.";

    public ServerException(int code, String text) {
        super(String.format(SERVER_ERROR_MSG_FORMAT, code, text));
    }

    public ServerException(String message) {
        super(message);
    }

}
