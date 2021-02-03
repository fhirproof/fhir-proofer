package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Search evaluator for date/time parameters
 */
public class DateEvaluator extends BaseEvaluator {

    protected static final String PARAM_TYPE = "date";

    public DateEvaluator(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Override
    protected List<String> getComparisonValues(Base base) {
        String query;
        if (base instanceof DateType) {
            DateParam dp = new DateParam(null, base.castToDate(base));
            query = dp.getValueAsQueryToken(fhirContext);
        } else if (base instanceof DateTimeType) {
            DateParam dp = new DateParam(null, base.castToDateTime(base));
            query = dp.getValueAsQueryToken(fhirContext);
        } else {
            query = "";
        }

        return Arrays.asList(query);
    }

    @Override
    protected boolean compare(String param, String query) {
        if (Character.isDigit(param.charAt(0))) {
            return param.equals(query);
        } else {
            String modifier = param.substring(0, 2);
            String justDate = param.substring(2);
            if (modifier.equals("gt")) {
                return justDate.compareTo(query) < 0;
            }
            if (modifier.equals("ge")) {
                return justDate.compareTo(query) <= 0;
            }
            if (modifier.equals("lt")) {
                return justDate.compareTo(query) > 0;
            }
            if (modifier.equals("le")) {
                return justDate.compareTo(query) >= 0;
            }
        }
        return false;
    }

    @Override
    public String getParameterType() {
        return PARAM_TYPE;
    }
}
