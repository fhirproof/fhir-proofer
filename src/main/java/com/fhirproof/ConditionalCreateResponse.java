package com.fhirproof;

/**
 * This is the output of the Conditional Create function of the FHIR Proof store.
 */
public class ConditionalCreateResponse {
    private final String response;
    private final int status;

    /**
     * Constructs a new response
     * @param response Response content
     * @param status Response HTTP status code
     */
    public ConditionalCreateResponse(String response, int status) {
        this.response = response;
        this.status = status;
    }

    /**
     * Gets the conditional create response body
     * @return the conditional create response body
     */
    public String getResponse() {
        return response;
    }

    /**
     * Gets the conditional create HTTP response status
     * @return the conditional create HTTP response status
     */
    public int getStatus() {
        return status;
    }
}
