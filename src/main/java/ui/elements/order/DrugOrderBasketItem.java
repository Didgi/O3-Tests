package ui.elements.order;

import com.codeborne.selenide.SelenideElement;
import ui.elements.BaseElement;
import ui.models.DrugOrderData;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byTagAndText;

public class DrugOrderBasketItem extends BaseElement {
    public DrugOrderBasketItem(SelenideElement element) {
        super(element);
    }

    private final SelenideElement status =
            find("[role='status']");

    private final SelenideElement dosage =
            find(byTagAndText("span", "DOSE")).parent();

    private final SelenideElement indication =
            find(byTagAndText("span", "INDICATION")).parent();

    public DrugOrderBasketItem shouldHaveStatus(OrderStatus expected) {
        status.shouldHave(exactText(expected.value()));
        return this;
    }

    public DrugOrderBasketItem shouldMatch(DrugOrderData order) {
        element.shouldHave(
                visible,
                text(order.drugName())
        );

        dosage.shouldHave(
                text(order.dosage().amount() + " " + order.dosage().unit()),
                text(order.dosage().route()),
                text(order.dosage().frequency()),
                text("QUANTITY " + order.dispensing().quantity()
                        + " " + order.dispensing().unit())
        );

        int refills = Integer.parseInt(order.dispensing().refills());

        if (refills > 0) {
            dosage.shouldHave(text("REFILLS " + order.dispensing().refills()));
        } else {
            dosage.shouldNotHave(text("REFILLS"));
        }

        indication.shouldHave(text(order.indication()));

        return this;
    }
}
