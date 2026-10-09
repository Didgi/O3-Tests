package ui.conditions;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementsCondition;
import org.openqa.selenium.WebElement;
import ui.elements.order.PatientOrderRow;
import ui.models.ExpectedOrderRow;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;

public class ContainsOrder extends WebElementsCondition {
    private final ExpectedOrderRow expected;

    public ContainsOrder(
            ExpectedOrderRow expected
    ) {
        if (expected == null) {
            throw new IllegalArgumentException("Expected value cannot be null");
        }
        this.expected = expected;
    }

    @Override
    public String toString() {

        return "contains: order number: '" + expected.orderNumber() +
                "'; date of order: " + expected.dateOfOrder() +
                "'; order type: '" + expected.orderType() +
                "'; display name: '" + expected.displayName() +
                "'; priority: '" + expected.priority() +
                "'; orderer: '" + expected.orderer() +
                "'";
    }

    @Override
    public CheckResult check(
            Driver driver,
            List<WebElement> elements
    ) {
        List<String> actualOrders = new ArrayList<>();

        for (WebElement row : elements) {

            PatientOrderRow orderRow = new PatientOrderRow($(row));

            String orderNumber = orderRow.orderNumber();
            String dateOfOrder = orderRow.dateOfOrder();
            String orderType = orderRow.orderType();
            String displayName = orderRow.orderDisplayName();
            String priority = orderRow.priority();
            String orderer = orderRow.orderer();

            actualOrders.add(
                    "order=" + orderNumber +
                            "'; date of order='" + dateOfOrder +
                            "'; order type=" + orderType +
                            "'; display name='" + displayName +
                            "; priority='" + priority +
                            "'; orderer='" + orderer +
                            "'"
            );
            if (
                    expected.orderNumber().equals(orderNumber) &&
                            expected.priority().equalsIgnoreCase(priority) &&
                            expected.displayName().equals(displayName) &&
                            expected.orderType().equalsIgnoreCase(orderType) &&
                            expected.dateOfOrder().equals(dateOfOrder) &&
                            expected.orderer().equals(orderer)
            ) {
                return CheckResult.accepted();
            }
        }

        return CheckResult.rejected(
                "No row matches: " + this,
                actualOrders
        );
    }
}
