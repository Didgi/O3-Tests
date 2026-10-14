package ui.models;

import java.util.Objects;

public record DrugOrderData(
        String drugName,
        Dosage dosage,
        Dispensing dispensing,
        String indication
) {

    public record Dosage(
            String amount,
            String unit,
            String route,
            String frequency
    ) {
        public Dosage {
            Objects.requireNonNull(amount);
            Objects.requireNonNull(unit);
            Objects.requireNonNull(route);
            Objects.requireNonNull(frequency);
        }
    }

    public record Dispensing(
            String quantity,
            String unit,
            String refills
    ) {
    }
}