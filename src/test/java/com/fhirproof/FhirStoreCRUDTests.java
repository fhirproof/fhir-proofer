package com.fhirproof;

import com.fhirproof.resources.TestFhirStore;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class FhirStoreCRUDTests {

    @Test
    public void create_test() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Patient newPat =
                new Patient()
                        .setGender(Enumerations.AdministrativeGender.OTHER)
                        .setBirthDate(DateType.parseV3("20200101").getValue())
                        .addName(
                                new HumanName()
                                        .setFamily("Dummy")
                                        .addGiven("Crash")
                                        .addGiven("Test")
                                        .addSuffix("Esq."))
                        .addIdentifier(
                                new Identifier()
                                        .setValue("1x1x1x1x1x1x1x1x1")
                                        .setSystem("http://identifiers.com/mrn")
                                        .setUse(Identifier.IdentifierUse.OFFICIAL))
                        .addAddress(
                                new Address().addLine("123 Some Street").setCity("Testington").setState("TT"));

        String id = store.create(newPat);
        Patient storedPat = store.read("Patient", id);

        Assert.assertEquals(id, storedPat.getIdElement().getIdPart());
        Assert.assertEquals(newPat.getGender(), storedPat.getGender());
        Assert.assertEquals(newPat.getBirthDate(), storedPat.getBirthDate());
        Assert.assertTrue(newPat.getAddressFirstRep().equalsDeep(storedPat.getAddressFirstRep()));
        Assert.assertTrue(newPat.getIdentifierFirstRep().equalsDeep(storedPat.getIdentifierFirstRep()));
        Assert.assertTrue(newPat.getNameFirstRep().equalsDeep(storedPat.getNameFirstRep()));
    }

    @Test
    public void update_test() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Patient newPat =
                new Patient()
                        .setGender(Enumerations.AdministrativeGender.OTHER)
                        .setBirthDate(DateType.parseV3("20200101").getValue())
                        .addName(
                                new HumanName()
                                        .setFamily("Dummy")
                                        .addGiven("Crash")
                                        .addGiven("Test")
                                        .addSuffix("Esq."))
                        .addIdentifier(
                                new Identifier()
                                        .setValue("1x1x1x1x1x1x1x1x1")
                                        .setSystem("http://identifiers.com/mrn")
                                        .setUse(Identifier.IdentifierUse.OFFICIAL))
                        .addAddress(
                                new Address().addLine("123 Some Street").setCity("Testington").setState("TT"));

        Address address = new Address().addLine("321 Other Street").setCity("Townvilleberg");

        String id = store.create(newPat);

        newPat.setAddress(Arrays.asList(address));
        newPat.setId(id);
        store.update("Patient", newPat);

        Patient storedPat = store.read("Patient", id);

        Assert.assertEquals(id, storedPat.getIdElement().getIdPart());
        Assert.assertEquals(newPat.getGender(), storedPat.getGender());
        Assert.assertEquals(newPat.getBirthDate(), storedPat.getBirthDate());
        Assert.assertTrue(newPat.getAddressFirstRep().equalsDeep(storedPat.getAddressFirstRep()));
        Assert.assertTrue(newPat.getIdentifierFirstRep().equalsDeep(storedPat.getIdentifierFirstRep()));
        Assert.assertTrue(newPat.getNameFirstRep().equalsDeep(storedPat.getNameFirstRep()));
    }

    @Test
    public void update_test_string() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Patient newPat =
                new Patient()
                        .setGender(Enumerations.AdministrativeGender.OTHER)
                        .setBirthDate(DateType.parseV3("20200101").getValue())
                        .addName(
                                new HumanName()
                                        .setFamily("Dummy")
                                        .addGiven("Crash")
                                        .addGiven("Test")
                                        .addSuffix("Esq."))
                        .addIdentifier(
                                new Identifier()
                                        .setValue("1x1x1x1x1x1x1x1x1")
                                        .setSystem("http://identifiers.com/mrn")
                                        .setUse(Identifier.IdentifierUse.OFFICIAL))
                        .addAddress(
                                new Address().addLine("123 Some Street").setCity("Testington").setState("TT"));

        Address address = new Address().addLine("321 Other Street").setCity("Townvilleberg");

        String id = store.create(newPat);

        newPat.setAddress(Arrays.asList(address));
        newPat.setId(id);
        store.update("Patient", TestFhirStore.PARSER.encodeResourceToString(newPat));

        Patient storedPat = store.read("Patient", id);

        Assert.assertEquals(id, storedPat.getIdElement().getIdPart());
        Assert.assertEquals(newPat.getGender(), storedPat.getGender());
        Assert.assertEquals(newPat.getBirthDate(), storedPat.getBirthDate());
        Assert.assertTrue(newPat.getAddressFirstRep().equalsDeep(storedPat.getAddressFirstRep()));
        Assert.assertTrue(newPat.getIdentifierFirstRep().equalsDeep(storedPat.getIdentifierFirstRep()));
        Assert.assertTrue(newPat.getNameFirstRep().equalsDeep(storedPat.getNameFirstRep()));
    }

    @Test
    public void delete_test() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Patient newPat =
                new Patient()
                        .setGender(Enumerations.AdministrativeGender.OTHER)
                        .setBirthDate(DateType.parseV3("20200101").getValue())
                        .addName(
                                new HumanName()
                                        .setFamily("Dummy")
                                        .addGiven("Crash")
                                        .addGiven("Test")
                                        .addSuffix("Esq."))
                        .addIdentifier(
                                new Identifier()
                                        .setValue("1x1x1x1x1x1x1x1x1")
                                        .setSystem("http://identifiers.com/mrn")
                                        .setUse(Identifier.IdentifierUse.OFFICIAL))
                        .addAddress(
                                new Address().addLine("123 Some Street").setCity("Testington").setState("TT"));

        String id = store.create(newPat);
        store.delete("Patient", id);
        Assert.assertFalse(store.store().get("Patient").containsKey(id));
    }

    @Test(expected = FhirProofException.class)
    public void delete_test_not_found() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        store.delete("Patient", "id");
    }

    @Test(expected = FhirProofException.class)
    public void update_test_not_found() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        store.update("Patient", "id");
    }

    @Test(expected = FhirProofException.class)
    public void update_test_bad_resource() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        store.update("pppppp", TestFhirStore.PARSER.encodeResourceToString(new Patient()));
    }

    @Test(expected = FhirProofException.class)
    public void update_test_bad_json() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        store.update("Patient", "szdvasg");
    }

    @Test(expected = FhirProofException.class)
    public void read_test_not_found() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        store.read("Patient", "szdvasg");
    }
}
