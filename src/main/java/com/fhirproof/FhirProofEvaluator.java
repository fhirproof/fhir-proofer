package com.fhirproof;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.TypeDetails;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

import java.util.List;

public class FhirProofEvaluator implements FHIRPathEngine.IEvaluationContext {

    private FhirProofStore store;

    public FhirProofEvaluator(FhirProofStore store) {
        this.store = store;
    }

    @Override
    public Base resolveConstant(Object o, String s, boolean b) throws PathEngineException {
        throw new NotImplementedException();
    }

    @Override
    public TypeDetails resolveConstantType(Object o, String s) throws PathEngineException {
        throw new NotImplementedException();
    }

    @Override
    public boolean log(String s, List<Base> list) {
        return false;
    }

    @Override
    public FunctionDetails resolveFunction(String s) {
        throw new NotImplementedException();
    }

    @Override
    public TypeDetails checkFunction(Object o, String s, List<TypeDetails> list)
            throws PathEngineException {
        throw new NotImplementedException();
    }

    @Override
    public List<Base> executeFunction(Object o, String s, List<List<Base>> list) {
        throw new NotImplementedException();
    }

    @Override
    public Base resolveReference(Object o, String s) throws FHIRException {
        // Verify a reference against the store
        String[] parts = s.split("/");
        return store.store().get(parts[0]).get(parts[1]);
    }

    @Override
    public boolean conformsToProfile(Object o, Base base, String s) throws FHIRException {
        return false;
    }

    @Override
    public ValueSet resolveValueSet(Object o, String s) {
        throw new NotImplementedException();
    }
}
