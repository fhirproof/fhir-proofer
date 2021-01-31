package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Reference;

import java.util.Arrays;
import java.util.List;

public class ReferenceEvaluator extends BaseEvaluator {

    protected static final String PARAM_TYPE = "reference";

    public ReferenceEvaluator(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Override
    protected List<String> getComparisonValues(Base base) throws Exception {
        Reference ref = base.castToReference(base);
        ReferenceParam rp = new ReferenceParam(ref.getReference());
        return Arrays.asList(rp.getValueAsQueryToken(fhirContext));
    }

    @Override
    protected boolean compare(String param, String query) {
        if (param.contains("/") || param.startsWith("http")) {
            return param.equals(query);
        } else {
            int index = query.indexOf('/');
            if (index >= 0) {
                return param.equals(query.substring(index + 1));
            } else {
                return param.equals(query);
            }
        }
    }

    @Override
    public String getParameterType() {
        return PARAM_TYPE;
    }
}
