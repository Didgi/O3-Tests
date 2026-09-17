package api.models.appointment;

public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    CHECKED_IN("CheckedIn"),
    COMPLETED("Completed");

    private final String value;

    AppointmentStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}