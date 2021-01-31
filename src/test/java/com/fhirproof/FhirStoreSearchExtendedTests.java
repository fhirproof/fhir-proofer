package com.fhirproof;

import com.fhirproof.resources.TestFhirStore;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FhirStoreSearchExtendedTests {

    @Test
    public void testRevInclude() throws IOException, FhirProofException {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle bundle = store.search("Patient", "identifier=3333333&_revinclude=Encounter:subject");
        assertNotNull("Bundle was null", bundle);
        assertEquals("Bundle does not contain 2 items", 2, bundle.getEntry().size());

        assertTrue("Expected encounter not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_ENC_3333333)));
        assertTrue("Expected patient not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_PAT_3333333)));
    }

    @Test
    public void testRevIncludeMultiplePrimaryMatches() throws IOException, FhirProofException {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle bundle = store.search("Patient", "gender=female&_revinclude=Encounter:subject");
        assertNotNull("Bundle was null", bundle);
        assertEquals("Bundle does not contain 2 items", 6, bundle.getEntry().size());

        assertTrue("Expected Patient (123457) not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_PAT_1234567)));
        assertTrue("Expected Patient (7654321) not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_PAT_7654321)));
        assertTrue("Expected Patient(9090909) not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_PAT_9090909)));
        assertTrue("Expected Encounter (1234567 Ambulatory) not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_ENC_1234567_AMB)));
        assertTrue("Expected Encounter (1234567) not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_ENC_1234567_HH)));
        assertTrue("Expected Encounter (9090909) not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_ENC_9090909)));
    }

    @Test
    public void testRevIncludeMultipleRevincludes() throws IOException, FhirProofException {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle bundle =
                store.search("Patient", "identifier=1234567&_revinclude=Encounter:subject&_revinclude=Observation:patient");
        assertNotNull("Bundle was null", bundle);
        assertEquals("Bundle does not contain 4 items", 4, bundle.getEntry().size());

        assertTrue("Expected Patient not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_PAT_1234567)));
        assertTrue("Expected Observation not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_OBS_1234567_BLOOD)));
        assertTrue("Expected Encounter not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_ENC_1234567_HH)));
        assertTrue("Expected Ambulatory Encounter not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_ENC_1234567_AMB)));

    }

    @Test
    public void testRevIncludeNoIncludeHits() throws IOException, FhirProofException {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle bundle = store.search("Patient", "identifier=2222222&_revinclude=Observation:patient");
        assertNotNull("Bundle was null", bundle);
        assertEquals("Bundle does not contain 1 item", 1, bundle.getEntry().size());

        assertTrue("Expected Patient not found", bundle.getEntry().stream().anyMatch(e ->
                e.getResource().getIdElement().getIdPart().equals(TestFhirStore.ID_PAT_2222222)));
    }

    @Test
    public void testRevIncludeDistinctMatching() throws IOException, FhirProofException {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle bundle =
                store.search("Encounter", String.format("patient=%s&_revinclude=Provenance:target", TestFhirStore.ID_PAT_1234567));

        assertNotNull("Bundle is not null", bundle);
        assertTrue("Bundle has entries", bundle.hasEntry());
        assertEquals("Bundle has 3 entries", 3, bundle.getEntry().size());

        HashMap<String, String> expectedMap = new HashMap<String, String>() {
            {
                put("Encounter", TestFhirStore.ID_ENC_1234567_AMB);
                put("Encounter", TestFhirStore.ID_ENC_1234567_HH);
                put("Provenance", TestFhirStore.ID_PROV_TWO_ENCS);
            }
        };

        for (Map.Entry<String, String> expected : expectedMap.entrySet()) {
            assertTrue(String.format("Bundle contains entry for %s/%s ", expected.getKey(), expected.getValue())
                    , bundle.getEntry().stream().anyMatch(e -> e.getResource().getIdElement().getIdPart().equals(expected.getValue())));
        }
    }
}
