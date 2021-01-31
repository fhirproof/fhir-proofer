package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Base;

import java.util.HashMap;
import java.util.List;

public abstract class BaseEvaluator implements ISearchEvaluator {

    protected FhirContext fhirContext;

    public BaseEvaluator(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    @Override
    public boolean evaluate(List<Base> bases, List<String> ands, List<String> ors) throws Exception {

        if (bases.size() == 0) {
            // if the path engine returned no elements then this is not a match
            return false;
        }

        /*
        we need to loop through and validate all the 'and' conditions are satisfied.
        we'll do that by populating a KVP-ing to map the evaluation result and then loop
        on all the elements (Base) that the path engine returned.
        */
        HashMap<String, Boolean> andMap = new HashMap<>();
        ands.stream().forEach(a -> andMap.put(a, false));
        for (String and : ands) {
            for (Base base : bases) {
                List<String> comparisonValues = getComparisonValues(base);

                if (comparisonValues.stream().anyMatch(v -> compare(and, v))) {
                    andMap.replace(and, true);
                }
            }
        }

        // Do the same as above except with the 'or' conditions.
        HashMap<String, Boolean> orMap = new HashMap<>();
        ors.stream().forEach(o -> orMap.put(o, false));
        for (String or : ors) {
            for (Base base : bases) {
                List<String> comparisonValues = getComparisonValues(base);

                if (comparisonValues.stream().anyMatch(v -> compare(or, v))) {
                    orMap.replace(or, true);
                }
            }
        }

        /*
          validate that all the 'and' conditions, if any, in the KVP map are all true
            (i.e. every required condition was satisfied)

          validate that at least one of the 'or' conditions, if any, in the MVP map is true
            (i.e. at least one of the 'or' conditions was satisfied)

          if both validations are true, then the Resource this Base came from matches the query
        */
        return ((ands.size() == 0 || andMap.values().stream().allMatch(a -> a))
                && (ors.size() == 0 || orMap.values().stream().anyMatch(o -> o)));
    }

    // the implementations of these should generate a URL query string-like comparison condition
    protected abstract List<String> getComparisonValues(Base base) throws Exception;

    // execute the type specific comparison logic
    protected abstract boolean compare(String param, String query);
}
