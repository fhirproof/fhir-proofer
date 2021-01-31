package com.fhirproof;

public class FhirProofException extends Exception {
    public FhirProofException(String message) {
        super(message);
    }

    public FhirProofException(Throwable throwable) {
        super(throwable.getMessage());
        this.setStackTrace(throwable.getStackTrace());
    }
}
