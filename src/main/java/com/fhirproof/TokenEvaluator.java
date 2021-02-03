package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.*;

import java.util.Arrays;
import java.util.List;

/**
 * Search evaluator for token parameters
 */
public class TokenEvaluator extends BaseEvaluator {

    protected static final String PARAM_TYPE = "token";

    public TokenEvaluator(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Override
    protected List<String> getComparisonValues(Base base) throws Exception {
        TokenParam tp;
        if (base instanceof CodeableConcept) {
            tp = new TokenParam(base.castToCodeableConcept(base).getCodingFirstRep());
        } else if (base instanceof Identifier) {
            Identifier identifier = base.castToIdentifier(base);
            tp = new TokenParam(identifier.getSystem(), identifier.getValue());
        } else if (base instanceof ContactPoint) {
            ContactPoint contact = base.castToContactPoint(base);
            tp = new TokenParam(contact.getSystem().getSystem(), contact.getValue());
        } else if (base instanceof BooleanType) {
            tp = new TokenParam(base.castToBoolean(base).getValueAsString());
        } else if (base instanceof IdType) {
            IdType id = base.castToId(base);
            tp = new TokenParam(new Coding().setCode(id.getIdPart()));
        } else {
            tp = new TokenParam(base.castToCoding(base));
        }
        String query = tp.getValueAsQueryToken(fhirContext);
        return Arrays.asList(query);
    }

    @Override
    protected boolean compare(String param, String query) {
        if (param.startsWith("|")) {
            return param.substring(1).equals(query);
        } else if (param.endsWith("|")) {
            String psys = param.substring(0, param.length() - 1);
            int index = query.indexOf('|');
            if (index < 0) {
                return false;
            }
            String qsub = query.substring(0, index);
            return psys.equals(qsub);

        } else if (param.contains("|")) {
            return param.equals(query);
        } else {
            int index = query.indexOf('|');
            String qsub = query.substring(index + 1);
            return param.equals(qsub);
        }
    }

    @Override
    public String getParameterType() {
        return PARAM_TYPE;
    }
}
