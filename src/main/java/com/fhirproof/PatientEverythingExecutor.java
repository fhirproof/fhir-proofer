package com.fhirproof;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * Performs the Patient $everything FHIR operation against the FHIR store.
 */
public class PatientEverythingExecutor implements IOperationExecutor<Bundle> {

    @Override
    public Bundle execute(
            String path,
            String operation,
            String params,
            HashMap<String, HashMap<String, Resource>> store)
            throws FhirProofException {

        String[] parts = path.split("/");
        Bundle bundle = new Bundle();
        for (String resource : store.keySet()) {
            for (Resource r : store.get(resource).values()) {
                Reference subject = getFieldValue(r, "subject", Reference.class);
                if (subject != null) {

                    if (subject.getReferenceElement().getIdPart().equals(parts[parts.length - 1])) {
                        bundle.addEntry().setResource(r).copy();
                    }
                }

                Reference patient = getFieldValue(r, "patient", Reference.class);
                if (patient != null) {

                    if (patient.getReferenceElement().getIdPart().equals(parts[parts.length - 1])) {
                        bundle.addEntry().setResource(r).copy();
                    }
                }

                List<Reference> refs = (List<Reference>) getFieldValue(r, "target", List.class);
                if (refs != null) {

                    for (Reference target : refs) {
                        if (target.getReferenceElement().getIdPart().equals(parts[parts.length - 1])) {
                            bundle.addEntry().setResource(r).copy();
                        }
                    }
                }

                List<Patient.PatientLinkComponent> links =
                        (List<Patient.PatientLinkComponent>) getFieldValue(r, "link", List.class);
                if (links != null) {
                    for (Patient.PatientLinkComponent link : links) {
                        if (link.getOther().getReferenceElement().getIdPart().equals(parts[parts.length - 1])) {
                            bundle.addEntry().setResource(r).copy();
                        }
                    }
                }

                if (r instanceof Patient && r.getIdElement().getIdPart().equals(parts[parts.length - 1])) {
                    bundle.addEntry().setResource(r).copy();
                }
            }
        }
        bundle.setTotal(bundle.getEntry().size());
        return bundle;
    }

    private <T> T getFieldValue(Resource resource, String field, Class<T> clazz)
            throws FhirProofException {
        Class resourceClass = resource.getClass();
        do {
            try {
                Field f = resourceClass.getDeclaredField(field);
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                T value = (T) f.get(resource);
                f.setAccessible(accessible);
                return value;
            } catch (NoSuchFieldException nsfex) {
                // do nothing
            } catch (Exception ex) {
                throw new FhirProofException(ex);
            }
            resourceClass = resourceClass.getSuperclass();
        } while (resourceClass != null);
        return null;
    }

    @Override
    public String getOperationName() {
        return "everything";
    }
}
