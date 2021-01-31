package com.fhirproof;

import com.fhirproof.resources.TestFhirStore;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FhirStoreSearchTests {

    @Test
    public void string_search_specific_field() throws IOException, FhirProofException {
        String query = "given=frank";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_3333333);

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
    public void string_search_specific_field_or_condition() throws IOException, FhirProofException {
        String query = "given=frank,f.";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_3333333);

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
    public void string_search_specific_field_and_or_conditions() throws IOException, FhirProofException {
        String query = "given=frank,franky.&given=f.";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_3333333);
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
    public void string_search_specific_field_and_or_conditions_no_results()
            throws IOException, FhirProofException {
        String query = "given=frank,franky&given=z.";
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

        query = "given=franklin,franky&given=f.";
        resource = "Patient";
        bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        expectedIds = Arrays.asList();
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
    public void string_search_general_field() throws IOException, FhirProofException {
        String query = "name=doe";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_PAT_9090909,
                        TestFhirStore.ID_PAT_1657934,
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
    public void number_search_exact() throws IOException, FhirProofException {
        String query = "probability=22.1";
        String resource = "RiskAssessment";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_RISK_22_1_1, TestFhirStore.ID_RISK_22_1_2);

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
    public void number_search_greater() throws IOException, FhirProofException {
        String query = "probability=gt22.1";
        String resource = "RiskAssessment";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_RISK_62_6, TestFhirStore.ID_RISK_84_0);

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
    public void number_search_greater_or_equal() throws IOException, FhirProofException {
        String query = "probability=ge22.1";
        String resource = "RiskAssessment";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_RISK_22_1_2,
                        TestFhirStore.ID_RISK_22_1_1,
                        TestFhirStore.ID_RISK_62_6,
                        TestFhirStore.ID_RISK_84_0);

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
    public void number_search_less() throws IOException, FhirProofException {
        String query = "probability=lt62.6";
        String resource = "RiskAssessment";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_RISK_22_1_2, TestFhirStore.ID_RISK_22_1_1, TestFhirStore.ID_RISK_1_22);

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
    public void number_search_less_or_equal() throws IOException, FhirProofException {
        String query = "probability=le22.1";
        String resource = "RiskAssessment";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_RISK_22_1_1, TestFhirStore.ID_RISK_22_1_2, TestFhirStore.ID_RISK_1_22);

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
    public void number_search_not_equal() throws IOException, FhirProofException {
        String query = "probability=ne22.1";
        String resource = "RiskAssessment";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_RISK_1_22, TestFhirStore.ID_RISK_62_6, TestFhirStore.ID_RISK_84_0);

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
    public void number_search_bad_boundary() throws IOException, FhirProofException {
        String query = "probability=zz22.1";
        String resource = "RiskAssessment";
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

    @Test
    public void date_search_exact() throws IOException, FhirProofException {
        String query = "date=1963-05-26T12:13:14-05:00";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_OBS_1675934_NO);

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
    public void date_search_less_than() throws IOException, FhirProofException {
        String query = "date=lt1963-05-26T12:13:14-05:00";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_OBS_3333333_NO, TestFhirStore.ID_OBS_1234567_BLOOD);

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
    public void date_search_less_than_or_equal() throws IOException, FhirProofException {
        String query = "date=le1963-05-26T12:13:14-05:00";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(
                        TestFhirStore.ID_OBS_3333333_NO,
                        TestFhirStore.ID_OBS_1234567_BLOOD,
                        TestFhirStore.ID_OBS_1675934_NO);

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
    public void date_search_greater_than() throws IOException, FhirProofException {
        String query = "date=gt1963-05-26T12:13:14-05:00";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_OBS_9090909_GLUCOSE);

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
    public void date_search_greater_than_or_equal() throws IOException, FhirProofException {
        String query = "date=ge1963-05-26T12:13:14-05:00";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_OBS_9090909_GLUCOSE, TestFhirStore.ID_OBS_1675934_NO);

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
    public void date_search_only_date() throws IOException, FhirProofException {
        String query = "birthdate=1909-04-12";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_3333333);

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
    public void date_search_only_year() throws IOException, FhirProofException {
        String query = "birthdate=1887";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_1234567);

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
    public void date_search_bad_boundary() throws IOException, FhirProofException {
        String query = "birthdate=zz1900-01-01";
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

    @Test
    public void reference_search_general() throws IOException, FhirProofException {
        String query = "subject=Patient/" + TestFhirStore.ID_PAT_1234567;
        String resource = "Encounter";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_ENC_1234567_AMB, TestFhirStore.ID_ENC_1234567_HH);

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
    public void reference_search_typed() throws IOException, FhirProofException {
        String query = "patient=" + TestFhirStore.ID_PAT_1234567;
        String resource = "Encounter";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_ENC_1234567_AMB, TestFhirStore.ID_ENC_1234567_HH);

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
    public void reference_search_no_results() throws IOException, FhirProofException {
        String query = "patient=fake-patient";
        String resource = "Encounter";
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

    @Test
    public void token_search_codeable_code_only() throws IOException, FhirProofException {
        String query = "code=15074-8";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_OBS_9090909_GLUCOSE, TestFhirStore.ID_OBS_1234567_BLOOD);

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
    public void token_search_codeable_code_and_system() throws IOException, FhirProofException {
        String query = "code=http://loinc.org|15074-8";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_OBS_9090909_GLUCOSE);

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
    public void token_search_codeable_system_only() throws IOException, FhirProofException {
        String query = "code=http://loinc.org|";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_OBS_9090909_GLUCOSE);

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
    public void token_search_codeable_code_no_system() throws IOException, FhirProofException {
        String query = "code=|123456789";
        String resource = "Observation";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds =
                Arrays.asList(TestFhirStore.ID_OBS_1675934_NO, TestFhirStore.ID_OBS_3333333_NO);

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
    public void token_search_identifier() throws IOException, FhirProofException {
        String query = "identifier=2222222";
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
    public void token_search_contact_point() throws IOException, FhirProofException {
        String query = "telecom=507-555-9876";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_1657934);

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
    public void token_search_boolean() throws IOException, FhirProofException {
        String query = "active=false";
        String resource = "Patient";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PAT_1234567);

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
    public void token_search_coding() throws IOException, FhirProofException {
        String query = "class=HH";
        String resource = "Encounter";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_ENC_1234567_HH);

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
    public void arrayed_element_multiple_and_params_search() throws IOException, FhirProofException {
        String query =
                String.format(
                        "target=Patient/%s&target=Patient/%s",
                        TestFhirStore.ID_PAT_2222222, TestFhirStore.ID_PAT_3333333);
        String resource = "Provenance";
        Bundle bundle = TestFhirStore.getFhirStoreCopy().search(resource, query);

        List<String> expectedIds = Arrays.asList(TestFhirStore.ID_PROV_TWO_PATS);

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
    public void arrayed_element_multiple_and_params_search_no_matches()
            throws IOException, FhirProofException {
        String query =
                String.format(
                        "target=Patient/%s&target=RiskAssessment/%s",
                        TestFhirStore.ID_PAT_2222222, TestFhirStore.ID_RISK_1_22);
        String resource = "Provenance";
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

    @Test
    public void multi_element_multiple_and_params_search() throws IOException, FhirProofException {
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
}
