package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Base;

import java.util.HashMap;
import java.util.List;

/**
 * The base class for the FHIR search evaluators. Extend this to create a custom method for evaluating searches.
 */
public abstract class BaseEvaluator implements ISearchEvaluator {

    protected FhirContext fhirContext;

    /**
     * Constructs an instance of the evaluator
     * @param fhirContext FHIR version context
     */
    public BaseEvaluator(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    /**
     * Evaluates the search condition
     * @param bases Base object to evaluate against
     * @param ands List of 'and' conditions
     * @param ors List of 'or' conditions
     * @return True of the Base object satisfies the conditions
     * @throws Exception Indicating the underlying failure
     */
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

    /**
     * Extracts the appropriate values to compare against from the Base object.
     * @param base Base object to get values from
     * @return List of comparison values
     * @throws Exception Indicator of underlying failure
     */
    // the implementations of these should generate a URL query string-like comparison condition
    protected abstract List<String> getComparisonValues(Base base) throws Exception;

    /**
     * Executes the search type specific comparison logic.
     * @param param Individual query parameter
     * @param query Value to evaluate
     * @return True if the value satisfies the condition
     */
    // execute the type specific comparison logic
    protected abstract boolean compare(String param, String query);
}
