package ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ui.elements.CreateVisitPanel;
import ui.elements.PatientAsideElement;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byTagAndText;
import static com.codeborne.selenide.Selenide.$;
import static ui.pages.UiPath.PATIENT_CHART;

public class PatientChartPage extends BasePage<PatientChartPage> {

    private final String patientUuid;

    private final SelenideElement patientBanner = $("[data-extension-id='patient-banner']");
    private final SelenideElement actionsButton = patientBanner.$("button[id^='patient-actions-menu-trigger-']");
    private final SelenideElement editPatientDetailsButton = patientBanner.$("[data-extension-id='edit-patient-details-button'] button");
    private final SelenideElement addVisitButton = patientBanner.$("[data-extension-id='start-visit-button'] button");
    private final SelenideElement recordVitalsButton = $("[data-extension-id='vitals-overview-widget']").$$("button").findBy(exactText("Record vital signs"));
    private final SelenideElement activeVisitBage = $(byTagAndText("span", "Active Visit"));

    public PatientChartPage() {
        this.patientUuid = null;
    }

    public PatientChartPage(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @Override
    public String url() {
        if (patientUuid == null) {
            throw new IllegalStateException(
                    "Patient UUID is required to open Patient Chart directly"
            );
        }

        return PATIENT_CHART.formatted(patientUuid);
    }

    @Step("Проверяем, что открыта карточка пациента")
    public PatientChartPage checkPatientChartOpened() {
        getPatientAside()
                .getPatientBanner()
                .checkPatientBannerVisible();

        return this;
    }

    public PatientAsideElement getPatientAside() {
        return new PatientAsideElement();
    }

    @Step("Переходим к созданию visit")
    public CreateVisitPanel openCreateVisitPanel() {
        actionsButton.click();
        addVisitButton.click();
        return new CreateVisitPanel();
    }

    @Step("Проверяем, что визит активен")
    public PatientChartPage checkActiveVisitBage() {
        activeVisitBage.shouldBe(visible);
        return this;
    }
}