package com.fhirproof;

import com.fhirproof.resources.TestFhirStore;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PatientEverythingOperationTests {

    private static FhirProofStore STORE;

    @BeforeClass
    public static void setup() throws  FhirProofException {
        STORE = TestFhirStore.getFhirStoreCopy();
        STORE.addExecutor(new PatientEverythingExecutor());
    }

    @Test
    public void patient_everything() throws FhirProofException {
        Bundle bundle =
                STORE.executeOperation(
                        "Patient/" + TestFhirStore.ID_PAT_1234567, "everything", null);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_PAT_1234567,
                        TestFhirStore.ID_ENC_1234567_AMB,
                        TestFhirStore.ID_ENC_1234567_HH,
                        TestFhirStore.ID_OBS_1234567_BLOOD,
                        TestFhirStore.ID_PAT_7654321,
                        TestFhirStore.ID_PROV_TWO_ENCS,
                        TestFhirStore.ID_ALLERGY_1234567);
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected operation result for: %s",
                                TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
    }
}
