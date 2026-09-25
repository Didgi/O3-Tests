package api.models.observations;

public enum UpdatedObservationStatus {
    AMENDED("AMENDED");
    private final String value;

    UpdatedObservationStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
