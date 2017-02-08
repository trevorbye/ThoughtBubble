package com.trevorbye.POJO;

public class ErrorJsonResponse extends HALResource {
    private String error;

    public ErrorJsonResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "ErrorJsonResponse{" +
                "error='" + error + '\'' +
                '}';
    }
}
