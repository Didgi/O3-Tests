package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ui.conditions.ContainsOrder;
import ui.elements.order.DrugOrderForm;
import ui.elements.order.OrderBasketWorkspace;
import ui.elements.PatientWorkspaceMenu;
import ui.models.ExpectedOrderRow;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PatientOrderPage extends BasePage<PatientOrderPage> {
    private final String patientUuid;
    private static final String PATH = "/patient/%s/chart/orders";

    private final SelenideElement orderDashboard =
            $("[data-extension-id='patient-orders-dashboard']");
    private final SelenideElement recordOrderBtn =
            orderDashboard.$$("button").findBy(exactText("Record orders"));
    private final SelenideElement ordersTable =
            orderDashboard.$("table");
    private final ElementsCollection tableRows =
            ordersTable.$$("tbody > tr[data-parent-row='true']");
    private final ElementsCollection snackbars =
            $$(".omrs-snackbars-container [role='alertdialog']");

    private final PatientWorkspaceMenu workspaceMenu = new PatientWorkspaceMenu();

    public PatientOrderPage(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @Override
    public String url() {
        return PATH.formatted(patientUuid);
    }

    public PatientOrderPage checkPatientOrderOpen() {
        orderDashboard.shouldBe(visible);
        return this;
    }

    public OrderBasketWorkspace openOrderBasket() {
        workspaceMenu.openOrderBasket();
        return new OrderBasketWorkspace();
    }

    public PatientOrderPage shouldContainExistingOrder(ExpectedOrderRow expected) {

        ContainsOrder containsOrder = new ContainsOrder(expected);
        tableRows.should(containsOrder);

        return this;
    }

    public OrderBasketWorkspace startOrderCancellation(String orderNumber) {
        SelenideElement menu = openActionsMenuForOrder(orderNumber);

        menu.$$("button")
                .findBy(exactText("Cancel order"))
                .click();

        return new OrderBasketWorkspace()
                .checkBasketFormIsOpened();
    }

    public PatientOrderPage shouldShowOrderUpdated(String drugName) {
        SelenideElement snackbar = snackbars
                .findBy(text("Order updated"))
                .shouldBe(visible, Duration.ofSeconds(5));

        snackbar
                .$(".cds--actionable-notification__title")
                .shouldHave(exactText("Order updated"));

        snackbar
                .$(".cds--actionable-notification__subtitle")
                .shouldHave(exactText("Updated " + drugName + "."));

        snackbar.shouldHave(
                cssClass("cds--actionable-notification--success"));

        return this;
    }

    public DrugOrderForm<PatientOrderPage> openOrderForEditing(
            String orderNumber
    ) {
        SelenideElement menu = openActionsMenuForOrder(orderNumber);

        menu.$$("button")
                .findBy(exactText("Modify order"))
                .click();

        return new DrugOrderForm<>(() -> this);
    }

    private SelenideElement openActionsMenuForOrder(String orderNumber) {
        SelenideElement row = findOrderRow(orderNumber);

        row.$("button[aria-haspopup='true']")
                .shouldBe(visible, enabled)
                .click();

        return $$("[role='menu'][aria-label='Actions menu']")
                .findBy(visible)
                .shouldBe(visible);
    }

    private SelenideElement findOrderRow(String orderNumber) {
        return ordersTable
                .$$("tbody > tr[data-parent-row='true'] td")
                .findBy(exactText(orderNumber))
                .closest("tr")
                .shouldBe(visible);
    }

}
