package ui.elements;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ui.pages.PatientEditPage;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.page;

public class PatientBannerElement extends BaseElement {

    private final SelenideElement actionsButton =
            find("button[id^='patient-actions-menu-trigger-']");

    private final SelenideElement editPatientDetailsButton =
            find("[data-extension-id='edit-patient-details-button'] button");

    public PatientBannerElement(SelenideElement element) {
        super(element);
    }

    @Step("Проверяем, что баннер пациента отображается")
    public PatientBannerElement checkPatientBannerVisible() {
        element.shouldBe(visible);
        return this;
    }

    @Step("Проверяем имя пациента: {givenName} {familyName}")
    public PatientBannerElement checkPatientName(
            String givenName,
            String familyName
    ) {
        String fullName = "%s %s".formatted(givenName, familyName);

        findAll("span")
                .findBy(exactText(fullName))
                .shouldBe(visible);

        return this;
    }

    @Step("Проверяем имя пациента: {givenName} {middleName} {familyName}")
    public PatientBannerElement checkPatientName(
            String givenName,
            String middleName,
            String familyName
    ) {
        String fullName = "%s %s %s".formatted(
                givenName,
                middleName,
                familyName
        );

        findAll("span")
                .findBy(exactText(fullName))
                .shouldBe(visible);

        return this;
    }

    @Step("Переходим к редактированию данных пациента")
    public PatientEditPage openEditPatientDetails() {
        actionsButton.click();
        editPatientDetailsButton.shouldBe(visible).click();

        return page(PatientEditPage.class)
                .checkPatientEditOpened();
    }
}