package ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class PatientEditPage extends BasePage<PatientEditPage> {

    private final SelenideElement pageTitle = $(byText("Edit patient details"));
    private final SelenideElement givenNameField = $("#givenName");
    private final SelenideElement updatePatientButton = $("button[type='submit']");
    private final SelenideElement cancelButton = $(byText("Cancel"));

    private final SelenideElement discardChangesModal = $("div[role='dialog']");
    private final SelenideElement discardChangesTitle = discardChangesModal.$(byText("Are you sure you want to discard these changes?"));
    private final SelenideElement discardChangesButton = discardChangesModal.$$("button").findBy(text("Discard"));

    @Override
    public String url() {
        throw new UnsupportedOperationException("Patient Edit Page is opened from Patient Chart");
    }

    @Step("Проверяем, что открыта страница редактирования пациента")
    public PatientEditPage checkPatientEditOpened() {
        pageTitle.shouldBe(visible);
        return this;
    }

    @Step("Изменяем имя пациента на: {givenName}")
    public PatientEditPage inputGivenName(String givenName) {
        givenNameField.click();
        givenNameField.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        givenNameField.sendKeys(Keys.BACK_SPACE);
        givenNameField.shouldHave(exactValue(""));

        givenNameField.sendKeys(givenName);
        givenNameField.shouldHave(exactValue(givenName));

        return this;
    }

    @Step("Сохраняем изменения пациента")
    public PatientChartPage updatePatient() {
        updatePatientButton.click();
        return getPage(PatientChartPage.class).checkPatientChartOpened();
    }

    @Step("Отменяем редактирование пациента")
    public PatientEditPage cancelEditing() {
        cancelButton.click();
        return this;
    }

    @Step("Проверяем confirmation об отмене изменений")
    public PatientEditPage checkDiscardChangesModal() {
        discardChangesModal.shouldBe(visible);
        discardChangesTitle.shouldBe(visible);
        return this;
    }

    @Step("Подтверждаем отмену изменений")
    public PatientChartPage discardChanges() {
        discardChangesButton.shouldBe(visible).click();
        return getPage(PatientChartPage.class).checkPatientChartOpened();
    }
}