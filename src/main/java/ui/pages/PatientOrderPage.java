package ui.pages;

import com.codeborne.selenide.SelenideElement;
import ui.elements.order.OrderBasketWorkspace;
import ui.elements.PatientWorkspaceMenu;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class PatientOrderPage extends BasePage<PatientOrderPage> {
    private final String patientUuid;
    private static final String PATH = "/patient/%s/chart/orders";

    private final SelenideElement orderDashboard =
            $("[data-extension-id='patient-orders-dashboard']");
    private final SelenideElement recordOrderBtn =
            orderDashboard.$$("button").findBy(exactText("Record orders"));

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

}
