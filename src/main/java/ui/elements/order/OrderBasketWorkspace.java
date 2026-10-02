package ui.elements.order;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ui.elements.BaseElement;
import ui.models.DrugOrderData;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byTagAndText;
import static com.codeborne.selenide.Selenide.$;

public class OrderBasketWorkspace extends BaseElement {

    private final SelenideElement drugOrderPanel =
            find("[data-extension-id='drug-order-panel']");
    private final SelenideElement addDrugOrderButton =
            drugOrderPanel.find(byTagAndText("button", "Add"));
    private final ElementsCollection drugOrderItems =
            drugOrderPanel.$$("[class*='__orderBasketItemTile___']");
    private final SelenideElement submitButton =
            findAll("button")
                    .findBy(exactText("Sign and close"));

    public OrderBasketWorkspace(SelenideElement element) {
        super(element);
    }

    public OrderBasketWorkspace() {
        this($("#order-basket"));
    }

    public OrderBasketWorkspace checkBasketFormIsOpened() {
        element.shouldBe(visible);
        return this;
    }

    public DrugOrderBasketItem findDrugOrderByName(String drugName) {
        SelenideElement item = drugOrderItems
                .findBy(text(drugName))
                .shouldBe(visible);
        return new DrugOrderBasketItem(item);
    }

    public DrugOrderWorkspace clickAddDrugOrder() {
        addDrugOrderButton.click();
        return new DrugOrderWorkspace();
    }

    public OrderBasketWorkspace shouldHaveDrugOrder(
            DrugOrderData order,
            OrderStatus status
    ) {
        findDrugOrderByName(order.drugName())
                .shouldHaveStatus(status)
                .shouldMatch(order);

        return this;
    }

    public OrderBasketWorkspace shouldHaveDrugOrder(
            String drugName,
            OrderStatus status
    ) {
        findDrugOrderByName(drugName)
                .shouldHaveStatus(status);

        return this;
    }

    public void signAndClose() {
        submitButton.click();
        element.should(disappear);
    }
}
