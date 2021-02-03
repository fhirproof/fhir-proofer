package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.StringType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search evaluator for string parameters
 */
public class StringEvaluator extends BaseEvaluator {

    protected static final String PARAM_TYPE = "string";

    public StringEvaluator(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Override
    protected List<String> getComparisonValues(Base base) throws Exception {
        List<String> values = new ArrayList<>();
        if (base instanceof StringType) {
            values.add(base.castToString(base).getValue());
        } else {

            // some string searches are actually against complex objects that need to have their actual
            // string primitive fields extracted for comparison
            for (Field stringField :
                    Arrays.stream(base.getClass().getDeclaredFields())
                            .filter(f -> f.getType().equals(StringType.class))
                            .collect(Collectors.toList())) {
                stringField.setAccessible(true);
                StringType value = (StringType) stringField.get(base);
                if (value != null) values.add(value.getValue());
            }

            // also check for n..* type elements that should be added to the comparison list
            for (Field list :
                    Arrays.stream(base.getClass().getDeclaredFields())
                            .filter(f -> f.getType().equals(List.class))
                            .collect(Collectors.toList())) {
                ParameterizedType actualType = (ParameterizedType) list.getGenericType();
                if (actualType.getActualTypeArguments()[0].equals(StringType.class)) {
                    list.setAccessible(true);
                    Object obj = list.get(base);
                    if (obj == null) continue;
                    for (StringType value : (ArrayList<StringType>) obj) {
                        if (value != null) values.add(value.getValue());
                    }
                }
            }
        }
        return values;
    }

    @Override
    protected boolean compare(String param, String query) {

        return param.equalsIgnoreCase(query);
    }

    @Override
    public String getParameterType() {
        return PARAM_TYPE;
    }
}
