package com.fhirproof;

import org.hl7.fhir.r4.model.Base;

import java.util.List;

public interface ISearchEvaluator {
    boolean evaluate(List<Base> base, List<String> ands, List<String> ors) throws Exception;

    String getParameterType();
}
