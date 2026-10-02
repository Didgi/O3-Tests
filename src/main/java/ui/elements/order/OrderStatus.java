package ui.elements.order;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderStatus {
    NEW("New"),
    DISCONTINUE("Discontinue");
    private final String value;

    public String value() {
        return value;
    }
}
