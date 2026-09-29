package api.models.order;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderAction {
    NEW("new");
    private final String value;

    public String value() {
        return this.value;
    }
}
