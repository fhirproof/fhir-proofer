package com.fhirproof;

import com.fhirproof.resources.TestFhirStore;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FhirStoreGeneralTests {

    @Test
    public void or_search() throws IOException, FhirProofException {
        String query = "name=doe,franklin";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_PAT_3333333,
                        TestFhirStore.ID_PAT_1657934,
                        TestFhirStore.ID_PAT_9090909,
                        TestFhirStore.ID_PAT_7654321);
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected search result for query '%s': %s",
                                query, TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
    }

    @Test
    public void and_search() throws IOException, FhirProofException {
        String query = "name=doe&name=jane";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_PAT_9090909, TestFhirStore.ID_PAT_7654321);
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected search result for query '%s': %s",
                                query, TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
    }

    @Test
    public void combination_search() throws IOException, FhirProofException {
        String query = "name=doe&gender=female";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_PAT_9090909, TestFhirStore.ID_PAT_7654321);
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected search result for query '%s': %s",
                                query, TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
    }

    @Test
    public void general_search_no_results() throws IOException, FhirProofException {
        String query = "given=asdfasdc";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected search result for query '%s': %s",
                                query, TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
    }

    @Test(expected = FhirProofException.class)
    public void general_search_bad_param() throws IOException, FhirProofException {
        String query = "fake=1";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected search result for query '%s': %s",
                                query, TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
    }

    @Test(expected = FhirProofException.class)
    public void no_search_evaluator() throws Exception {
        FhirProofStore store = TestFhirStore.getFhirStoreCopy();
        Field field = store.getClass().getDeclaredField("evaluators");
        field.setAccessible(true);
        field.set(store, new HashMap<>());

        String query = "name=doe";
        String resource = "Patient";
        store.search(resource, query);
    }

    @Test
    public void search_by_id() throws Exception {
        String query = "_id=" + TestFhirStore.ID_PAT_2222222;
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);
        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_2222222);

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            String id = entry.getResource().getIdElement().getIdPart();
            if (!expectedIds.stream().anyMatch(i -> id.equals(i))) {
                Assert.fail(
                        String.format(
                                "Unexpected search result for query '%s': %s",
                                query, TestFhirStore.PARSER.encodeResourceToString(entry.getResource())));
            }
        }
        Assert.assertEquals(expectedIds.size(), bundle.getTotal());
        Assert.assertEquals(expectedIds.size(), bundle.getEntry().size());
    }

    @Test
    public void search_by_language_unsupported() {
        try {
            String query = "_language=" + TestFhirStore.ID_PAT_2222222;
            String resource = "Patient";
            TestFhirStore.getFhirStoreCopy().search(resource, query);
        } catch (Exception ex) {
            Assert.assertEquals("Universal parameter of '_language' is not supported", ex.getMessage());
        }
    }
}
