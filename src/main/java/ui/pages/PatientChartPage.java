package ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static ui.pages.UiPath.PATIENT_CHART;

public class PatientChartPage extends BasePage<PatientChartPage> {

    private final String patientUuid;

    private final SelenideElement patientBanner = $("[data-extension-id='patient-banner']");
    private final SelenideElement actionsButton = patientBanner.$("button[id^='patient-actions-menu-trigger-']");
    private final SelenideElement editPatientDetailsButton = patientBanner.$("[data-extension-id='edit-patient-details-button'] button");
    private final SelenideElement recordVitalsButton = $("[data-extension-id='vitals-overview-widget']").$$("button").findBy(exactText("Record vital signs"));

    public PatientChartPage() {
        this.patientUuid = null;
    }

    public PatientChartPage(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @Override
    public String url() {
        if (patientUuid == null) {
            throw new IllegalStateException("Patient UUID is required to open Patient Chart directly");
        }

        return PATIENT_CHART.formatted(patientUuid);
    }

    @Step("Проверяем, что открыта карточка пациента")
    public PatientChartPage checkPatientChartOpened() {
        patientBanner.shouldBe(visible);
        return this;
    }

    @Step("Проверяем имя пациента: {givenName} {familyName}")
    public PatientChartPage checkPatientName(String givenName, String familyName) {
        String fullName = "%s %s".formatted(givenName, familyName);

        patientBanner
                .$$("span")
                .findBy(exactText(fullName))
                .shouldBe(visible);

        return this;
    }

    @Step("Проверяем имя пациента: {givenName} {middleName} {familyName}")
    public PatientChartPage checkPatientName(String givenName, String middleName, String familyName) {
        String fullName = "%s %s %s".formatted(givenName, middleName, familyName);

        patientBanner
                .$$("span")
                .findBy(exactText(fullName))
                .shouldBe(visible);

        return this;
    }

    @Step("Переходим к редактированию данных пациента")
    public PatientEditPage openEditPatientDetails() {
        actionsButton.click();
        editPatientDetailsButton.shouldBe(visible).click();
        return getPage(PatientEditPage.class).checkPatientEditOpened();
    }

    @Step("Открываем форму записи жизненных показателей")
    public PatientChartPage clickRecordVitals() {
        recordVitalsButton.shouldBe(visible).click();
        return this;
    }
}