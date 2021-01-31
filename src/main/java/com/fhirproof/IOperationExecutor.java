package com.fhirproof;

import org.hl7.fhir.r4.model.Resource;

import java.util.HashMap;

public interface IOperationExecutor<T> {

    T execute(String path, String operation, String params, HashMap<String, HashMap<String, Resource>> store) throws FhirProofException;

    String getOperationName();
}
