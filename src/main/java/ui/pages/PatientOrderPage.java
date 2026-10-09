package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import ui.conditions.ContainsOrder;
import ui.elements.PatientWorkspaceMenu;
import ui.elements.ToastMessages;
import ui.elements.order.DrugOrderForm;
import ui.elements.order.OrderBasketWorkspace;
import ui.models.ExpectedOrderRow;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PatientOrderPage extends BasePage<PatientOrderPage> {
    private final String patientUuid;

    private final SelenideElement orderDashboard =
            $("[data-extension-id='patient-orders-dashboard']");
    private final SelenideElement ordersTable =
            orderDashboard.$("table");
    private final SelenideElement rangeDatePicker =
            orderDashboard.$(".cds--date-picker");
    private final SelenideElement startRangeDatePicker =
            rangeDatePicker.$("[slot='start']");
    private final SelenideElement endRangeDatePicker =
            rangeDatePicker.$("[slot='end']");
    private final ElementsCollection tableRows =
            ordersTable.$$("tbody > tr[data-parent-row='true']");

    private final PatientWorkspaceMenu workspaceMenu = new PatientWorkspaceMenu();

    public PatientOrderPage(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @Override
    public String url() {
        return UiPath.PATIENT_ORDER_PAGE.formatted(patientUuid);
    }

    public PatientOrderPage checkPatientOrderOpen() {
        orderDashboard.shouldBe(visible);
        return this;
    }

    public PatientOrderPage waitUntilLoaded() {
        orderDashboard.should(match("orders are loaded", element -> {
            boolean skeletonGone = element
                    .findElements(By.cssSelector(".cds--skeleton"))
                    .isEmpty();

            boolean hasOrders = element
                    .findElements(By.cssSelector(
                            "tbody > tr[data-parent-row='true']"
                    ))
                    .stream()
                    .anyMatch(WebElement::isDisplayed);

            boolean emptyState = element
                    .findElements(By.xpath(
                            ".//*[normalize-space()='There are no orders to display for this patient']"
                    ))
                    .stream()
                    .anyMatch(WebElement::isDisplayed);

            return skeletonGone && (hasOrders || emptyState);
        }), Duration.ofSeconds(10));

        return this;
    }

    public OrderBasketWorkspace openOrderBasket() {
        return workspaceMenu.openOrderBasket()
                .checkBasketFormIsOpened();
    }

    public PatientOrderPage shouldContainExistingOrder(
            ExpectedOrderRow expected,
            OffsetDateTime orderDate
    ) {
        String browserTimeZone = Selenide.executeJavaScript(
                "return Intl.DateTimeFormat().resolvedOptions().timeZone;"
        );

        String formattedDate = orderDate
                .atZoneSameInstant(ZoneId.of(Objects.requireNonNull(browserTimeZone, "Could not get browser timezone")))
                .format(DateTimeFormatter.ofPattern(
                        "dd-MMM-yyyy", Locale.UK
                ));
        ExpectedOrderRow expectedWithDate = expected.toBuilder()
                .dateOfOrder(formattedDate)
                .build();

        ContainsOrder containsOrder = new ContainsOrder(expectedWithDate);
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

    public DrugOrderForm<ToastMessages> openOrderForEditing(
            String orderNumber
    ) {
        SelenideElement menu = openActionsMenuForOrder(orderNumber);

        menu.$$("button")
                .findBy(exactText("Modify order"))
                .click();

        return new DrugOrderForm<>(ToastMessages::new);
    }

    public PatientOrderPage setFilterStartDate(OffsetDateTime start) {
        return setFilterDate(start, startRangeDatePicker);
    }

    public PatientOrderPage setFilterEndDate(OffsetDateTime end) {
        return setFilterDate(end, endRangeDatePicker);
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

    private PatientOrderPage setFilterDate(OffsetDateTime date, SelenideElement slot) {
        int month = date.getMonth().getValue();
        int dayOfMonth = date.getDayOfMonth();
        int year = date.getYear();
        slot.shouldBe(visible);
        slot.$("[data-type='day']")
                .setValue(String.valueOf(dayOfMonth));
        slot.$("[data-type='month']")
                .setValue(String.valueOf(month));
        slot.$("[data-type='year']")
                .setValue(String.valueOf(year));

        return this;
    }
}
