package ui.conditions;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementsCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import ui.models.ExpectedOrderRow;

import java.util.ArrayList;
import java.util.List;

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
        String dateDescription = expected.dateOfOrder() == null
                ? "not checked"
                : "'" + expected.dateOfOrder() + "'";

        return "contains: order number '" + expected.orderNumber() +
                "'; date of order: " + dateDescription +
                "; display name: '" + expected.displayName() +
                "'; priority: '" + expected.priority() + "'";
    }

    @Override
    public CheckResult check(
            Driver driver,
            List<WebElement> elements
    ) {
        List<String> actualOrders = new ArrayList<>();

        for (WebElement row : elements) {
            // TODO: Replace positional table cell indexes with a PatientOrderRow component.
            String orderNumber = row.findElements(By.tagName("td")).get(1).getText();
            String priority = row.findElements(By.tagName("td")).get(5).getText();
            String displayName = row.findElements(By.tagName("td")).get(4).getText();
            String dateOfOrder = row.findElements(By.tagName("td")).get(2).getText();
            actualOrders.add(
                    "order=" + orderNumber +
                            "; priority=" + priority +
                            " display name: " + displayName +
                            " date of order: " + dateOfOrder
            );
            if (
                    expected.orderNumber().equals(orderNumber) &&
                    expected.priority().equalsIgnoreCase(priority) &&
                    expected.displayName().equals(displayName) &&
                    (
                            expected.dateOfOrder() == null ||
                            expected.dateOfOrder().equals(dateOfOrder)
                    )
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
