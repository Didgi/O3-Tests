package api.testdata;

import api.config.Config;

public final class ReferenceTestData {
    public static String locationUuid() {
        return Config.getProperty("test_location_uuid");
    }

    public static String conceptId() {
        return Config.getProperty("weight_concept_id");
    }

    public static String patientIdentifierSourceUuid() {
        return Config.getProperty("patient_identifier_source_uuid");
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
}
