package ui.elements.order;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderStatus {
    NEW("New");
    private final String value;

    public String value() {
        return value;
    }
}
