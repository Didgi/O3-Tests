package api.models.order;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderUrgency {

    ROUTINE("Routine");
    private final String value;

    public String value() {
        return value;
    }
}
