package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.NumberParam;
import org.hl7.fhir.r4.model.Base;

import java.util.Arrays;
import java.util.List;

/**
 * Search evaluator for numeric parameters
 */
public class NumberEvaluator extends BaseEvaluator {

    protected static final String PARAM_TYPE = "number";

    public NumberEvaluator(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Override
    protected List<String> getComparisonValues(Base base) throws Exception {
        NumberParam np = new NumberParam(base.primitiveValue());
        String query = np.getValueAsQueryToken(fhirContext);
        return Arrays.asList(query);
    }

    @Override
    protected boolean compare(String param, String query) {
        if (Character.isAlphabetic(param.charAt(0))) {
            String modifier = param.substring(0, 2);
            String justNumber = param.substring(2);
            if (modifier.equals("gt")) {
                return justNumber.compareTo(query) < 0;
            }
            if (modifier.equals("ge")) {
                return justNumber.compareTo(query) <= 0;
            }
            if (modifier.equals("lt")) {
                return justNumber.compareTo(query) > 0;
            }
            if (modifier.equals("le")) {
                return justNumber.compareTo(query) >= 0;
            }
            if (modifier.equals("ne")) {
                return justNumber.compareTo(query) != 0;
            }

            return false;
        } else {
            return param.equals(query);
        }
    }

    @Override
    public String getParameterType() {
        return PARAM_TYPE;
    }
}
