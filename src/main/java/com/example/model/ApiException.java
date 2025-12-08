package com.example.model;

public class ApiException extends Exception {
    private final int statusCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public ApiException(String message, int statusCode) {
        super(message + "(HTTP " + statusCode + ")");
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
