package ui.elements;

import com.codeborne.selenide.SelenideElement;
import ui.elements.order.OrderBasketWorkspace;

import static com.codeborne.selenide.Selenide.$;

public class PatientWorkspaceMenu extends BaseElement {

    public PatientWorkspaceMenu() {
        super($("[data-extension-slot-name='patient-chart']"));
    }

    private final SelenideElement orderBasketButton =
            find("[data-extension-id='patient-chart-order-basket'] button");

    public OrderBasketWorkspace openOrderBasket() {
        orderBasketButton.click();
        return new OrderBasketWorkspace()
                .checkBasketFormIsOpened();
    }
}
