package api.testdata;

import api.config.Config;

public final class ReferenceTestData {
    public static String locationUuid() {
        return Config.getProperty("test_location_uuid");
    }

    public static String weightConceptId() {
        return Config.getProperty("weight_concept_id");
    }

    public static String textConceptId() {
        return Config.getProperty("text_concept_id");
    }

    public static String patientIdentifierSourceUuid() {
        return Config.getProperty("patient_identifier_source_uuid");
    }

    public static String patientUuid() {
        return Config.getProperty("test_patient_uuid");
    }

    public static String visitTypeUuid() {
        return Config.getProperty("test_visit_type_uuid");
    }

    public static String appointmentServiceUuid() {
        return Config.getProperty("test_appointment_service_uuid");
    }

    public static String appointmentKind() {
        return Config.getProperty("test_appointment_kind");
    }

    public static String timeZone() {
        return Config.getProperty("time_zone");
    }

    public static String dateTimeFormat() {
        return Config.getProperty("date_time_format");
    }

    public static String patientIdentifierTypeUuid() {
        return Config.getProperty("patient_identifier_type_uuid");
    }

    public static String heightConceptId() {
        return Config.getProperty("height_concept_id");
    }

    public static String encounterVitalsTypeUuid() {
        return Config.getProperty("encounter_vitals_type_uuid");
    }

    public static String consultationEncounterTypeUuid() {
        return Config.getProperty("consultation_encounter_type_uuid");
    }

    public static String encounterProviderUuid() {
        return Config.getProperty("encounter_provider_uuid");
    }

    public static String clinicianEncounterRoleUuid() {
        return Config.getProperty("clinician_encounter_role_uuid");
    }
}
