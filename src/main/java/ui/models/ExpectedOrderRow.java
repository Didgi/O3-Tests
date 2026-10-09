package ui.models;

import lombok.Builder;

@Builder(toBuilder = true)
public record ExpectedOrderRow(
        String orderNumber,
        String dateOfOrder,
        String orderType,
        String displayName,
        String priority,
        String orderer

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

        if (orderType == null) {
            throw new IllegalArgumentException("orderType cannot be null");
        }

        if (orderer == null) {
            throw new IllegalArgumentException("orderer cannot be null");
        }
    }

    public ExpectedOrderRow(
            String orderNumber,
            String orderType,
            String displayName,
            String priority,
            String orderer
    ) {
        this(orderNumber, null, orderType, displayName, priority, orderer);
    }
}
