package com.fhirproof;

public class ConditionalCreateResponse {
    private final String response;
    private final int status;

    public ConditionalCreateResponse(String response, int status) {
        this.response = response;
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public int getStatus() {
        return status;
    }
}
