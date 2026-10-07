package ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ui.elements.PatientEditForm;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class PatientEditPage extends BasePage<PatientEditPage> {

    private final SelenideElement pageTitle =
            $(byText("Edit patient details"));

    private final PatientEditForm patientEditForm =
            new PatientEditForm(
                    $("#givenName").closest("form")
            );

    private final SelenideElement discardChangesModal =
            $("div[role='dialog']");

    private final SelenideElement discardChangesTitle =
            discardChangesModal.$(
                    byText("Are you sure you want to discard these changes?")
            );

    private final SelenideElement discardChangesButton =
            discardChangesModal
                    .$$("button")
                    .findBy(text("Discard"));

    @Override
    public String url() {
        throw new UnsupportedOperationException(
                "Patient Edit Page is opened from Patient Chart"
        );
    }

    @Step("Проверяем, что открыта страница редактирования пациента")
    public PatientEditPage checkPatientEditOpened() {
        pageTitle.shouldBe(visible);
        patientEditForm.checkPatientEditFormOpened();

        return this;
    }

    @Step("Изменяем имя пациента на: {givenName}")
    public PatientEditPage inputGivenName(String givenName) {
        patientEditForm.inputGivenName(givenName);

        return this;
    }

    @Step("Сохраняем изменения пациента")
    public PatientChartPage updatePatient() {
        patientEditForm.submit();

        return getPage(PatientChartPage.class)
                .checkPatientChartOpened();
    }

    @Step("Отменяем редактирование пациента")
    public PatientEditPage cancelEditing() {
        patientEditForm.cancel();

        return this;
    }

    @Step("Проверяем окно подтверждения отмены изменений")
    public PatientEditPage checkDiscardChangesModal() {
        discardChangesModal.shouldBe(visible);
        discardChangesTitle.shouldBe(visible);

        return this;
    }

    @Step("Подтверждаем отмену изменений")
    public PatientChartPage discardChanges() {
        discardChangesButton
                .shouldBe(visible)
                .click();

        return getPage(PatientChartPage.class)
                .checkPatientChartOpened();
    }
}