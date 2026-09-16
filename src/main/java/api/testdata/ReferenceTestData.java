package api.testdata;

import api.config.Config;

public final class ReferenceTestData {
    public static String locationUuid() {
        return Config.getProperty("test_location_uuid");
    }

    public static String conceptId() {
        return Config.getProperty("concept_id");
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
}
