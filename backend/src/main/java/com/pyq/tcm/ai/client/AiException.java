package com.pyq.tcm.ai.client;

public class AiException extends RuntimeException {

    private final String errorCode;

    public AiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
