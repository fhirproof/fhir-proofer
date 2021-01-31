package com.fhirproof;

import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Test;

public class ResourceIsolationTests {

    @Test
    public void read_results_isolated() throws Exception {
        FhirProofStore store = new FhirProofStore();

        // set up
        Patient pat_original = new Patient().setGender(Enumerations.AdministrativeGender.MALE);
        String id = store.create(pat_original);

        // update original which should NOT affect the store's version
        pat_original.setBirthDateElement(DateType.parseV3("19991231"));
        Patient pat_read = store.read("Patient", id);
        Assert.assertFalse(pat_read.equalsDeep(pat_original));

        // read then update locally then re-read and check store was NOT updated
        Patient pat_read_1 = store.read("Patient", id);
        pat_read_1.setBirthDateElement(DateType.parseV3("19991231"));
        Patient pat_read_2 = store.read("Patient", id);
        Assert.assertFalse(pat_read_1.equalsDeep(pat_read_2));
    }

    @Test
    public void search_results_isolated() throws Exception {
        FhirProofStore store = new FhirProofStore();

        // set up
        Patient pat_original = new Patient().setGender(Enumerations.AdministrativeGender.MALE);
        String id = store.create(pat_original);

        // update original which should NOT affect the store's version
        pat_original.setBirthDateElement(DateType.parseV3("19991231"));
        Patient pat_search =
                (Patient) store.search("Patient", "_id=" + id).getEntryFirstRep().getResource();
        Assert.assertFalse(pat_search.equalsDeep(pat_original));

        // search then update locally then re-read and check store was NOT updated
        Patient pat_search_1 =
                (Patient) store.search("Patient", "_id=" + id).getEntryFirstRep().getResource();
        pat_search_1.setBirthDateElement(DateType.parseV3("19991231"));
        Patient pat_search_2 =
                (Patient) store.search("Patient", "_id=" + id).getEntryFirstRep().getResource();
        Assert.assertFalse(pat_search_1.equalsDeep(pat_search_2));
    }

    @Test
    public void updated_resources_isolated() throws Exception {
        FhirProofStore store = new FhirProofStore();

        // set up
        Patient pat_original = new Patient().setGender(Enumerations.AdministrativeGender.MALE);
        String id = store.create(pat_original);

        // update original which should NOT affect the store's version
        pat_original.setBirthDateElement(DateType.parseV3("19991231"));
        store.update("Patient", id, pat_original);
        pat_original.setBirthDateElement(DateType.parseV3("18881231"));
        Assert.assertFalse(store.store().get("Patient").get(id).equalsDeep(pat_original));
    }

    @Test
    public void created_resources_isolated() throws Exception {
        FhirProofStore store = new FhirProofStore();

        // set up
        Patient pat_original = new Patient().setGender(Enumerations.AdministrativeGender.MALE);
        String id = store.create(pat_original);

        // update original which should NOT affect the store's version
        pat_original.setBirthDateElement(DateType.parseV3("19991231"));
        Assert.assertFalse(store.store().get("Patient").get(id).equalsDeep(pat_original));
    }
}
