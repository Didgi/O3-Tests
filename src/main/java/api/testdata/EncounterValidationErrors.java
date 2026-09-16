package api.testdata;

public final class EncounterValidationErrors {

    public static final String PATIENT_FIELD = "patient";
    public static final String ENCOUNTER_TYPE_FIELD = "encounterType";
    public static final String ENCOUNTER_DATETIME_FIELD = "encounterDatetime";
    public static final String VISIT_FIELD = "visit";

    public static final String PATIENT_REQUIRED = "Encounter.error.patient.required";
    public static final String ENCOUNTER_TYPE_REQUIRED = "Encounter.error.encounterType.required";
    public static final String FUTURE_DATETIME = "Encounter.datetimeShouldBeBeforeCurrent";
    public static final String DATETIME_OUTSIDE_VISIT_RANGE = "Encounter.datetimeShouldBeInVisitDatesRange";
    public static final String PATIENT_VISIT_MISMATCH = "Encounter.visit.patients.dontMatch";

    private EncounterValidationErrors() {
    }
}
