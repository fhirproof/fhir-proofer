package com.fhirproof.resources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fhirproof.FhirProofStore;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.util.HashMap;

public class TestFhirStore {
    private static FhirProofStore STORE;

    public static IParser PARSER = FhirContext.forR4().newJsonParser();

    public static String ID_PAT_2222222;
    public static String ID_PAT_3333333;
    public static String ID_PAT_1234567;
    public static String ID_PAT_9090909;
    public static String ID_PAT_1657934;
    public static String ID_PAT_7654321;

    public static String ID_PRAC_DRACULA;
    public static String ID_PRAC_HELSING;
    public static String ID_PRAC_DR_NO;

    public static String ID_ENC_1234567_HH;
    public static String ID_ENC_1234567_AMB;
    public static String ID_ENC_1657934_VH;
    public static String ID_ENC_1675934;
    public static String ID_ENC_9090909;
    public static String ID_ENC_3333333;

    public static String ID_OBS_9090909_GLUCOSE;
    public static String ID_OBS_1234567_BLOOD;
    public static String ID_OBS_1675934_NO;
    public static String ID_OBS_3333333_NO;

    public static String ID_RISK_22_1_1;
    public static String ID_RISK_1_22;
    public static String ID_RISK_84_0;
    public static String ID_RISK_22_1_2;
    public static String ID_RISK_62_6;

    public static String ID_PROV_TWO_PATS;
    public static String ID_PROV_TWO_ENCS;

    public static String ID_ALLERGY_1234567;

    static {
        try {
            STORE = new FhirProofStore();

            Practitioner prac_dracula =
                    new Practitioner()
                            .addName(new HumanName().setFamily("Dracula").addPrefix("Count"))
                            .addAddress(new Address().setCountry("Transylvania").addLine("Castle Dracula"));
            ID_PRAC_DRACULA = STORE.create(prac_dracula);

            Practitioner prac_helsing =
                    new Practitioner()
                            .addName(new HumanName().setFamily("van Helsing").addGiven("Abraham"))
                            .addAddress(new Address().setCountry("Netherlands").setCity("Amsterdam"))
                            .addTelecom(
                                    new ContactPoint()
                                            .setSystem(ContactPoint.ContactPointSystem.OTHER)
                                            .setValue("VANHELMD")
                                            .setUse(ContactPoint.ContactPointUse.WORK));
            ID_PRAC_HELSING = STORE.create(prac_helsing);

            Practitioner prac_no =
                    new Practitioner()
                            .addName(
                                    new HumanName()
                                            .setFamily("No")
                                            .addGiven("Julius")
                                            .setUse(HumanName.NameUse.NICKNAME))
                            .addName(
                                    new HumanName()
                                            .setUse(HumanName.NameUse.OFFICIAL)
                                            .setFamily("Blofeld")
                                            .addGiven("Ernst")
                                            .addGiven("Stravo"));
            ID_PRAC_DR_NO = STORE.create(prac_no);

            Patient pat_liebowitz =
                    new Patient()
                            .addName(new HumanName().setFamily("Liebowitz").addGiven("Issac").addPrefix("St."))
                            .setGender(Enumerations.AdministrativeGender.MALE)
                            .setDeceased(new BooleanType(true))
                            .addIdentifier(
                                    new Identifier()
                                            .setSystem("http://hospit.al/terminology/systemid/ehr/medical_record_number")
                                            .setUse(Identifier.IdentifierUse.USUAL)
                                            .setValue("2222222"));
            ID_PAT_2222222 = STORE.create(pat_liebowitz);

            Patient pat_frank =
                    new Patient()
                            .addName(new HumanName().setFamily("Franklin").addGiven("Frank").addGiven("F."))
                            .setGender(Enumerations.AdministrativeGender.MALE)
                            .setBirthDateElement(DateType.parseV3("19090412"))
                            .addGeneralPractitioner(new Reference("Practitioner/" + ID_PRAC_DR_NO))
                            .addIdentifier(
                                    new Identifier()
                                            .setSystem("http://hospit.al/terminology/systemid/ehr/medical_record_number")
                                            .setUse(Identifier.IdentifierUse.USUAL)
                                            .setValue("3333333"));
            ID_PAT_3333333 = STORE.create(pat_frank);

            Patient pat_mina =
                    new Patient()
                            .addName(new HumanName().setFamily("Harker").addGiven("Mina"))
                            .setGender(Enumerations.AdministrativeGender.FEMALE)
                            .setBirthDateElement(DateType.parseV3("1887"))
                            .addGeneralPractitioner(new Reference("Practitioner/" + ID_PRAC_HELSING))
                            .addGeneralPractitioner(new Reference("Practitioner/" + ID_PRAC_DRACULA))
                            .setActive(false)
                            .addIdentifier(
                                    new Identifier()
                                            .setSystem("http://hospit.al/terminology/systemid/ehr/medical_record_number")
                                            .setUse(Identifier.IdentifierUse.USUAL)
                                            .setValue("1234567"));
            ID_PAT_1234567 = STORE.create(pat_mina);

            Patient pat_mina_merge =
                    new Patient()
                            .addName(
                                    new HumanName().setFamily("Doe").addGiven("Jane").addGiven("M.").addSuffix("Sr."))
                            .setGender(Enumerations.AdministrativeGender.FEMALE)
                            .addIdentifier(
                                    new Identifier()
                                            .setSystem("http://hospit.al/terminology/systemid/ehr/medical_record_number")
                                            .setUse(Identifier.IdentifierUse.USUAL)
                                            .setValue("7654321"))
                            .addLink(
                                    new Patient.PatientLinkComponent()
                                            .setOther(new Reference("Patient/" + ID_PAT_1234567)));
            ID_PAT_7654321 = STORE.create(pat_mina_merge);

            Patient pat_jane =
                    new Patient()
                            .addName(new HumanName().setFamily("Doe").addGiven("Jane"))
                            .setGender(Enumerations.AdministrativeGender.FEMALE)
                            .setBirthDateElement(DateType.parseV3("1980-03-09"))
                            .addTelecom(
                                    new ContactPoint()
                                            .setUse(ContactPoint.ContactPointUse.MOBILE)
                                            .setSystem(ContactPoint.ContactPointSystem.PHONE)
                                            .setValue("507-555-1234"))
                            .setActive(true)
                            .addIdentifier(
                                    new Identifier()
                                            .setSystem("http://hospit.al/terminology/systemid/ehr/medical_record_number")
                                            .setUse(Identifier.IdentifierUse.USUAL)
                                            .setValue("9090909"));
            ID_PAT_9090909 = STORE.create(pat_jane);

            Patient pat_john =
                    new Patient()
                            .addName(new HumanName().setFamily("Doe").addGiven("John"))
                            .setGender(Enumerations.AdministrativeGender.MALE)
                            .setBirthDateElement(DateType.parseV3("19551116"))
                            .addGeneralPractitioner(new Reference("Practitioner/" + ID_PRAC_HELSING))
                            .addTelecom(
                                    new ContactPoint()
                                            .setUse(ContactPoint.ContactPointUse.HOME)
                                            .setSystem(ContactPoint.ContactPointSystem.PHONE)
                                            .setValue("507-555-9876"))
                            .setActive(true)
                            .addIdentifier(
                                    new Identifier()
                                            .setSystem("http://hospit.al/terminology/systemid/ehr/medical_record_number")
                                            .setUse(Identifier.IdentifierUse.USUAL)
                                            .setValue("1657934"));
            ID_PAT_1657934 = STORE.create(pat_john);

            Encounter enc_mina_drac =
                    new Encounter()
                            .setSubject(new Reference("Patient/" + ID_PAT_1234567))
                            .setClass_(
                                    new Coding()
                                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                                            .setCode("HH")
                                            .setDisplay("Home Health"))
                            .setStatus(Encounter.EncounterStatus.FINISHED)
                            .addParticipant(
                                    new Encounter.EncounterParticipantComponent()
                                            .setIndividual(new Reference("Practitioner/" + ID_PRAC_DRACULA)));
            ID_ENC_1234567_HH = STORE.create(enc_mina_drac);

            Encounter enc_mina_vanh =
                    new Encounter()
                            .setSubject(new Reference("Patient/" + ID_PAT_1234567))
                            .setClass_(
                                    new Coding()
                                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                                            .setCode("AMB")
                                            .setDisplay("Ambulatory"))
                            .setStatus(Encounter.EncounterStatus.FINISHED)
                            .addParticipant(
                                    new Encounter.EncounterParticipantComponent()
                                            .setIndividual(new Reference("Practitioner/" + ID_PRAC_HELSING)));
            ID_ENC_1234567_AMB = STORE.create(enc_mina_vanh);

            Encounter enc_jane =
                    new Encounter()
                            .setSubject(new Reference("Patient/" + ID_PAT_9090909))
                            .setClass_(
                                    new Coding()
                                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                                            .setCode("AMB")
                                            .setDisplay("Ambulatory"))
                            .setStatus(Encounter.EncounterStatus.INPROGRESS)
                            .addParticipant(
                                    new Encounter.EncounterParticipantComponent()
                                            .setIndividual(new Reference("Practitioner/" + ID_PRAC_DR_NO)));
            ID_ENC_9090909 = STORE.create(enc_jane);

            Encounter enc_john_vh =
                    new Encounter()
                            .setSubject(new Reference("Patient/" + ID_PAT_1657934))
                            .setClass_(
                                    new Coding()
                                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                                            .setCode("AMB")
                                            .setDisplay("Ambulatory"))
                            .setStatus(Encounter.EncounterStatus.FINISHED)
                            .addParticipant(
                                    new Encounter.EncounterParticipantComponent()
                                            .setIndividual(new Reference("Practitioner/" + ID_PRAC_HELSING)));
            ID_ENC_1657934_VH = STORE.create(enc_john_vh);

            Encounter enc_john_no =
                    new Encounter()
                            .setSubject(new Reference("Patient/" + ID_PAT_1657934))
                            .setClass_(
                                    new Coding()
                                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                                            .setCode("AMB")
                                            .setDisplay("Ambulatory"))
                            .setStatus(Encounter.EncounterStatus.FINISHED)
                            .addParticipant(
                                    new Encounter.EncounterParticipantComponent()
                                            .setIndividual(new Reference("Practitioner/" + ID_PRAC_DR_NO)));
            ID_ENC_1675934 = STORE.create(enc_john_no);

            Encounter enc_frank_no =
                    new Encounter()
                            .setSubject(new Reference("Patient/" + ID_PAT_3333333))
                            .setClass_(
                                    new Coding()
                                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                                            .setCode("AMB")
                                            .setDisplay("Ambulatory"))
                            .setStatus(Encounter.EncounterStatus.FINISHED)
                            .addParticipant(
                                    new Encounter.EncounterParticipantComponent()
                                            .setIndividual(new Reference("Practitioner/" + ID_PRAC_DR_NO)));
            ID_ENC_3333333 = STORE.create(enc_frank_no);

            Observation obs_jane_glucose =
                    new Observation()
                            .setStatus(Observation.ObservationStatus.FINAL)
                            .setCode(
                                    new CodeableConcept()
                                            .addCoding(
                                                    new Coding()
                                                            .setSystem("http://loinc.org")
                                                            .setCode("15074-8")
                                                            .setDisplay("Glucose [Moles/volume] in Blood")))
                            .setSubject(new Reference("Patient/" + ID_PAT_9090909))
                            .setEffective(DateTimeType.parseV3("20200722064152-0500"))
                            .setValue(
                                    new Quantity()
                                            .setValue(6.3)
                                            .setUnit("mmol/l")
                                            .setSystem("http://unitsofmeasure.org")
                                            .setCode("mmol/l"))
                            .setEncounter(new Reference("Encounter/" + ID_ENC_9090909));
            ID_OBS_9090909_GLUCOSE = STORE.create(obs_jane_glucose);

            Observation obs_mina_vamp =
                    new Observation()
                            .setStatus(Observation.ObservationStatus.FINAL)
                            .setCode(
                                    new CodeableConcept()
                                            .addCoding(
                                                    new Coding()
                                                            .setSystem("http://vanhelsing.nl")
                                                            .setCode("15074-8")
                                                            .setDisplay("Vampiric Blood Test")))
                            .setSubject(new Reference("Patient/" + ID_PAT_1234567))
                            .setEffective(DateTimeType.parseV3("18950306192127+0000"))
                            .setValue(new SimpleQuantity().setValue(86.4))
                            .setEncounter(new Reference("Encounter/" + ID_ENC_1234567_AMB));
            ID_OBS_1234567_BLOOD = STORE.create(obs_mina_vamp);

            Observation obs_john =
                    new Observation()
                            .setStatus(Observation.ObservationStatus.FINAL)
                            .setCode(
                                    new CodeableConcept()
                                            .addCoding(new Coding().setCode("123456789").setDisplay("Some Test")))
                            .setSubject(new Reference("Patient/" + ID_PAT_1657934))
                            .setEffective(DateTimeType.parseV3("19630526121314-0500"))
                            .setValue(new SimpleQuantity().setValue(77))
                            .setEncounter(new Reference("Encounter/" + ID_ENC_1675934));
            ID_OBS_1675934_NO = STORE.create(obs_john);

            Observation obs_frank =
                    new Observation()
                            .setStatus(Observation.ObservationStatus.FINAL)
                            .setCode(
                                    new CodeableConcept()
                                            .addCoding(new Coding().setCode("123456789").setDisplay("Some Test")))
                            .setSubject(new Reference("Patient/" + ID_PAT_3333333))
                            .setEffective(DateTimeType.parseV3("19241202024358-0500"))
                            .setValue(new SimpleQuantity().setValue(23))
                            .setEncounter(new Reference("Encounter/" + ID_ENC_3333333));
            ID_OBS_3333333_NO = STORE.create(obs_frank);

            RiskAssessment risk_1 =
                    new RiskAssessment()
                            .addPrediction(
                                    new RiskAssessment.RiskAssessmentPredictionComponent()
                                            .setProbability(new DecimalType(22.1)))
                            .setSubject(new Reference("Patient/" + ID_PAT_2222222));
            ID_RISK_22_1_1 = STORE.create(risk_1);

            RiskAssessment risk_2 =
                    new RiskAssessment()
                            .addPrediction(
                                    new RiskAssessment.RiskAssessmentPredictionComponent()
                                            .setProbability(new DecimalType(1.22)))
                            .setSubject(new Reference("Patient/" + ID_PAT_2222222));
            ID_RISK_1_22 = STORE.create(risk_2);

            RiskAssessment risk_3 =
                    new RiskAssessment()
                            .addPrediction(
                                    new RiskAssessment.RiskAssessmentPredictionComponent()
                                            .setProbability(new DecimalType(84.0)))
                            .setSubject(new Reference("Patient/" + ID_PAT_2222222));
            ID_RISK_84_0 = STORE.create(risk_3);

            RiskAssessment risk_4 =
                    new RiskAssessment()
                            .addPrediction(
                                    new RiskAssessment.RiskAssessmentPredictionComponent()
                                            .setProbability(new DecimalType(22.1)))
                            .setSubject(new Reference("Patient/" + ID_PAT_2222222));
            ID_RISK_22_1_2 = STORE.create(risk_4);

            RiskAssessment risk_5 =
                    new RiskAssessment()
                            .addPrediction(
                                    new RiskAssessment.RiskAssessmentPredictionComponent()
                                            .setProbability(new DecimalType(62.6)))
                            .setSubject(new Reference("Patient/" + ID_PAT_2222222));
            ID_RISK_62_6 = STORE.create(risk_5);

            Provenance prov_two_pats =
                    new Provenance()
                            .addTarget(new Reference("Patient/" + ID_PAT_3333333))
                            .addTarget(new Reference("Patient/" + ID_PAT_2222222))
                            .addTarget(new Reference("Encounter/" + ID_ENC_3333333))
                            .setRecorded(DateTimeType.now().getValue())
                            .addAgent(
                                    new Provenance.ProvenanceAgentComponent()
                                            .setWho(
                                                    new Reference().setIdentifier(new Identifier().setValue("Unit Tests"))));
            ID_PROV_TWO_PATS = STORE.create(prov_two_pats);

            Provenance prov_two_enc =
                    new Provenance()
                            .addTarget(new Reference("Patient/" + ID_PAT_1234567))
                            .addTarget(new Reference("Encounter/" + ID_ENC_1234567_AMB))
                            .addTarget(new Reference("Encounter/" + ID_ENC_1234567_HH))
                            .setRecorded(DateTimeType.now().getValue())
                            .addAgent(
                                    new Provenance.ProvenanceAgentComponent()
                                            .setWho(
                                                    new Reference().setIdentifier(new Identifier().setValue("Unit Tests"))));
            ID_PROV_TWO_ENCS = STORE.create(prov_two_enc);

            AllergyIntolerance allergy_1234567 = new AllergyIntolerance()
                    .setPatient(new Reference("Patient/" + ID_PAT_1234567))
                    .setCode(new CodeableConcept().addCoding(
                            new Coding()
                                    .setSystem("http://cod.es/allergies")
                                    .setCode("G1")
                                    .setDisplay("Grapefruit")
                    ));
            ID_ALLERGY_1234567 = STORE.create(allergy_1234567);

        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private TestFhirStore() {
    }

    public static FhirProofStore getFhirStoreCopy() throws IOException {
        FhirProofStore newStore = new FhirProofStore();
        for (String resource : STORE.store().keySet()) {
            newStore.store().put(resource, new HashMap<>());
            for (String id : STORE.store().get(resource).keySet()) {
                newStore.store().get(resource).put(id, STORE.store().get(resource).get(id).copy());
            }
        }
        return newStore;
    }
}
