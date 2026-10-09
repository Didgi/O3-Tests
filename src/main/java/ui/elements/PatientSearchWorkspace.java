package ui.elements;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ui.pages.PatientChartPage;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class PatientSearchWorkspace extends BaseElement {

    public PatientSearchWorkspace() {
        super($("[data-extension-id='patient-search-icon']"));
    }

    private final SelenideElement searchPatientField =
            find("[data-testid='patientSearchBar']");

    private final SelenideElement searchButton =
            find("button[type='submit']");

    private final SelenideElement noSearchResultsMessage =
            $(byText("Sorry, no patient charts were found"));

    @Step("Проверяем, что поиск пациента открыт")
    public PatientSearchWorkspace checkPatientSearchOpened() {
        searchPatientField.shouldBe(visible);
        return this;
    }

    @Step("Ищем пациента: {query}")
    public PatientSearchWorkspace searchPatient(String query) {
        searchPatientField.setValue(query);
        searchButton.click();
        return this;
    }

    @Step("Открываем найденного пациента")
    public PatientChartPage openPatient(String patientUuid) {
        String patientLink = "a[href*='/patient/%s/chart']".formatted(patientUuid);
        $(patientLink)
                .shouldBe(visible)
                .click();
        return new PatientChartPage()
                .checkPatientChartOpened();
    }

    @Step("Проверяем, что пациенты не найдены")
    public PatientSearchWorkspace checkNoPatientsFound() {
        noSearchResultsMessage.shouldBe(visible);
        return this;
    }
}