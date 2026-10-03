package ui.models;

public record ExpectedOrderRow(
        String orderNumber,
        String priority,
        String displayName,
        String dateOfOrder
) {
    public ExpectedOrderRow {
        if (orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }

        if (priority == null) {
            throw new IllegalArgumentException("priority cannot be null");
        }

        if (displayName == null) {
            throw new IllegalArgumentException("displayName cannot be null");
        }
    }

    public ExpectedOrderRow(
            String orderNumber,
            String priority,
            String displayName
    ) {
        this(orderNumber, priority, displayName, null);
    }
}
