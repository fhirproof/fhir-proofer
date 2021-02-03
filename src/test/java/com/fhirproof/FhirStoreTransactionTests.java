package com.fhirproof;

import com.fhirproof.resources.TestFhirStore;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FhirStoreTransactionTests {

    @Test
    public void testBasicTransaction() throws IOException, FhirProofException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent req = new Bundle.BundleEntryRequestComponent().setUrl(
                "Patient/" + TestFhirStore.ID_PAT_2222222).setMethod(Bundle.HTTPVerb.GET);
        trans.addEntry().setRequest(req);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        Patient patient = (Patient) store.store().get("Patient").get(TestFhirStore.ID_PAT_2222222);

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("One transaction response", 1, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntryFirstRep();
        assertTrue("Transaction entry has expected resource", patient.equalsDeep(entry.getResource()));

        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", patient.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%sPatient/%s", FhirProofStore.FHIR_STORE_URL, TestFhirStore.ID_PAT_2222222), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "200 OK", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", patient.getMeta().getLastUpdated(), entryResponse.getLastModified());

    }

    @Test
    public void testMultipleRequestTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent req = new Bundle.BundleEntryRequestComponent().setUrl(
                "Patient/" + TestFhirStore.ID_PAT_2222222).setMethod(Bundle.HTTPVerb.GET);
        trans.addEntry().setRequest(req);

        Bundle.BundleEntryRequestComponent req2 = new Bundle.BundleEntryRequestComponent().setUrl(
                "Encounter/" + TestFhirStore.ID_ENC_3333333).setMethod(Bundle.HTTPVerb.GET);
        trans.addEntry().setRequest(req2);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        Patient patient = (Patient) store.store().get("Patient").get(TestFhirStore.ID_PAT_2222222);
        Encounter encounter = (Encounter) store.store().get("Encounter").get(TestFhirStore.ID_ENC_3333333);

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("Two transaction responses", 2, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
        assertEntryResponse(entry, "200 OK", patient, true);

        entry =
                response.getEntry().get(1);//this is OK here since it should be process FIFO, but bad practice in general
        assertEntryResponse(entry, "200 OK", encounter, true);
    }

//    @Test
//    public void testMultipleUpdatesTransaction() throws IOException, FhirProofException {
//        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
//
//        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
//        Bundle.BundleEntryRequestComponent req = new Bundle.BundleEntryRequestComponent().setUrl(
//                "Patient/" + TestFhirStore.ID_PAT_2222222).setMethod(Bundle.HTTPVerb.PUT);
//
//        Patient pat =
//                ((Patient) store.store().get("Patient").get(TestFhirStore.ID_PAT_2222222).copy()).addPhoto(new Attachment().setData("2222222 PHOTO DATA".getBytes()));
//        trans.addEntry().setRequest(req).setResource(pat);
//
//        Bundle.BundleEntryRequestComponent req2 = new Bundle.BundleEntryRequestComponent().setUrl(
//                "Encounter/" + TestFhirStore.ID_ENC_3333333).setMethod(Bundle.HTTPVerb.PUT);
//
//        Encounter enc =
//                ((Encounter) store.store().get("Encounter").get(TestFhirStore.ID_ENC_3333333).copy()).setPeriod(new Period().setStartElement(DateTimeType.now()));
//        trans.addEntry().setRequest(req2).setResource(enc);
//
//
//        Patient storePat = (Patient) store.store().get("Patient").get(TestFhirStore.ID_PAT_2222222);
//        Encounter storeEnc = (Encounter) store.store().get("Encounter").get(TestFhirStore.ID_ENC_3333333);
//
//        Bundle response = store.executeTransaction(trans);
//        assertNotNull("Transaction response exists", response);
//        assertEquals("Two transaction responses", 2, response.getEntry().size());
//
//        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
//        assertEntryResponse(entry, "200 OK", storePat, false);
//
//        entry =
//                response.getEntry().get(1);//this is OK here since it should be process FIFO, but bad practice in general
//        assertEntryResponse(entry, "200 OK", storeEnc, false);
//    }

    @Test
    public void testCreateTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST);
        Practitioner pract =
                new Practitioner().addIdentifier(new Identifier().setValue("777").setSystem("http://fake.org/ids/").setUse(Identifier.IdentifierUse.OFFICIAL)).addName(new HumanName().setFamily("Williams").addGiven("William"));
        trans.addEntry().setRequest(create).setResource(pract);

        Bundle.BundleEntryRequestComponent req2 = new Bundle.BundleEntryRequestComponent().setUrl(
                "Encounter/" + TestFhirStore.ID_ENC_3333333).setMethod(Bundle.HTTPVerb.GET);
        trans.addEntry().setRequest(req2);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        Encounter encounter = (Encounter) store.store().get("Encounter").get(TestFhirStore.ID_ENC_3333333);

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("Two transaction responses", 2, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
        String[] parts = entry.getResponse().getLocation().split("/");
        String id = parts[parts.length - 1];
        Resource storePract = store.store().get("Practitioner").get(id);

        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", storePract.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, storePract.getResourceType().name(), id), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "201 Created", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", storePract.getMeta().getLastUpdated(), entryResponse.getLastModified());

        entry =
                response.getEntry().get(1);//this is OK here since it should be process FIFO, but bad practice in general
        assertEntryResponse(entry, "200 OK", encounter, true);
    }

    @Test
    public void testConditionalCreateTransactionNoExistingRecord() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setIfNoneExist("identifier=777");
        Practitioner pract =
                new Practitioner().addIdentifier(new Identifier().setValue("777").setSystem("http://fake.org/ids/").setUse(Identifier.IdentifierUse.OFFICIAL)).addName(new HumanName().setFamily("Williams").addGiven("William"));
        trans.addEntry().setRequest(create).setResource(pract);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("Two transaction responses", 1, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
        String[] parts = entry.getResponse().getLocation().split("/");
        String id = parts[parts.length - 1];
        Resource storePract = store.store().get("Practitioner").get(id);

        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", storePract.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, storePract.getResourceType().name(), id), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "201 Created", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", storePract.getMeta().getLastUpdated(), entryResponse.getLastModified());

    }

    @Test
    public void testConditionalCreateTransactionExistingRecord() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setIfNoneExist("identifier=3333333");
        Patient pat =
                new Patient().addIdentifier(new Identifier().setValue("3333333").setSystem("http://fake.org/ids/").setUse(Identifier.IdentifierUse.OFFICIAL)).addName(new HumanName().setFamily("Williams").addGiven("William"));
        trans.addEntry().setRequest(create).setResource(pat);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        Patient existing = (Patient) store.store().get("Patient").get(TestFhirStore.ID_PAT_3333333);

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("Two transaction responses", 1, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();

        String[] parts = entry.getResponse().getLocation().split("/");
        String id = parts[parts.length - 1];

        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());

        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", existing.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, existing.getResourceType().name(), id), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "200 OK", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", existing.getMeta().getLastUpdated().toString(), entryResponse.getLastModified().toString());
    }

    @Test
    public void testCreateAndDeleteTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST);
        Practitioner pract =
                new Practitioner().addIdentifier(new Identifier().setValue("777").setSystem("http://fake.org/ids/").setUse(Identifier.IdentifierUse.OFFICIAL)).addName(new HumanName().setFamily("Williams").addGiven("William"));
        trans.addEntry().setRequest(create).setResource(pract);

        Bundle.BundleEntryRequestComponent pract2 = new Bundle.BundleEntryRequestComponent().setUrl(
                "Practitioner/" + TestFhirStore.ID_PRAC_DR_NO).setMethod(Bundle.HTTPVerb.DELETE);
        trans.addEntry().setRequest(pract2);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        Practitioner deleted = (Practitioner) store.store().get("Practitioner").get(TestFhirStore.ID_PRAC_DR_NO);

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("Two transaction responses", 2, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
        String[] parts = entry.getResponse().getLocation().split("/");
        String id = parts[parts.length - 1];
        Resource storePract = store.store().get("Practitioner").get(id);

        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", storePract.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, storePract.getResourceType().name(), id), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "201 Created", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", storePract.getMeta().getLastUpdated(), entryResponse.getLastModified());

        entry =
                response.getEntry().get(1);//this is OK here since it should be process FIFO, but bad practice in general
        entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", deleted.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, deleted.getResourceType().name(), deleted.getIdElement().getIdPart()), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "204 No Content", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", deleted.getMeta().getLastUpdated(), entryResponse.getLastModified());

    }

    @Test
    public void testCreateNoResourceTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST);
        trans.addEntry().setRequest(create);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        assertThrows("No resource was provided in a create bundle", FhirProofException.class, () -> store.executeTransaction(trans));
    }

    @Test
    public void testCreateNoTypeTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle();
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST);
        trans.addEntry().setRequest(create);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        assertThrows("No resource was provided in a create bundle", FhirProofException.class, () -> store.executeTransaction(trans));
    }

    @Test
    public void testCreateNoMethodTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent create =
                new Bundle.BundleEntryRequestComponent();
        trans.addEntry().setRequest(create);

        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        assertThrows("No resource was provided in a create bundle", FhirProofException.class, () -> store.executeTransaction(trans));
    }

    @Test
    public void testUpdateTransaction() throws IOException, FhirProofException {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        String photo = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        Patient updatedPat =
                ((Patient) store.read("Patient", TestFhirStore.ID_PAT_9090909)).addPhoto(new Attachment().setData(photo.getBytes()));

        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent update =
                new Bundle.BundleEntryRequestComponent().setUrl(
                        "Patient/" + TestFhirStore.ID_PAT_9090909).setMethod(Bundle.HTTPVerb.PUT);
        trans.addEntry().setRequest(update).setResource(updatedPat);


        Bundle response = store.executeTransaction(trans);
        assertNotNull("Transaction response exists", response);
        assertEquals("One transaction response", 1, response.getEntry().size());

        Bundle.BundleEntryComponent entry = response.getEntry().get(0);
        String[] parts = entry.getResponse().getLocation().split("/");
        String id = parts[parts.length - 1];
        Patient storePat = (Patient)store.store().get("Patient").get(id);

        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", storePat.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, storePat.getResourceType().name(), id), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", "200 OK", entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", storePat.getMeta().getLastUpdated(), entryResponse.getLastModified());
        assertTrue("updated patient has data", storePat.hasPhoto());
        assertTrue("Updated data matches", new String(storePat.getPhotoFirstRep().getData()).equals(photo));

    }

    @Test
    public void testEmptyTransaction() throws IOException, FhirProofException {
        Bundle trans = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        trans.setEntry(new ArrayList<>());
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();

        Bundle response = store.executeTransaction(trans);
        assertNotNull("Response bundle exists", response);
        assertEquals("Response is a Transaction-Response type", Bundle.BundleType.TRANSACTIONRESPONSE.toCode(), response.getType().toCode());
        assertFalse("Response has no entries", response.hasEntry());
    }

    private void assertEntryResponse(Bundle.BundleEntryComponent entry, String status, Resource expectedResource, boolean checkResource) {
        if (checkResource) {
            assertTrue("Transaction entry has expected resource", expectedResource.equalsDeep(entry.getResource()));
        }

        assertTrue("Transaction entry has response", entry.hasResponse());
        Bundle.BundleEntryResponseComponent entryResponse = entry.getResponse();
        assertTrue("Transaction response has eTag", entryResponse.hasEtag());
        assertTrue("Transaction response has Last Modified", entryResponse.hasLastModified());
        assertTrue("Transaction response has Location", entryResponse.hasLocation());
        assertTrue("Transaction response has Status", entryResponse.hasStatus());
        assertEquals("Transaction response eTag matches", String.format("W/\"%s\"", expectedResource.getMeta().getVersionId()), entryResponse.getEtag());
        assertEquals("Transaction response Location matches", String.format("%s%s/%s", FhirProofStore.FHIR_STORE_URL, expectedResource.getResourceType().name(), expectedResource.getIdElement().getIdPart()), entryResponse.getLocation());
        assertEquals("Transaction response Status matches", status, entryResponse.getStatus());
        assertEquals("Transaction response Last Update matches", expectedResource.getMeta().getLastUpdated().toString(), entryResponse.getLastModified().toString());
    }
}
