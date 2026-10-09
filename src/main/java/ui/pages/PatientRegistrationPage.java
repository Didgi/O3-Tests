package ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ui.elements.DatePicker;

import java.time.LocalDate;
import java.time.Period;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class PatientRegistrationPage extends BasePage<PatientRegistrationPage> {

    private final SelenideElement givenNameField = $("#givenName");
    private final SelenideElement familyNameField = $("#familyName");
    private final SelenideElement maleGender = $("label[for='gender-option-male']");
    private final SelenideElement femaleGender = $("label[for='gender-option-female']");
    private final SelenideElement estimatedYearsField = $("#yearsEstimated");
    private final SelenideElement estimatedMonthsField = $("#monthsEstimated");

    private final DatePicker birthDatePicker = new DatePicker("birthdate");

    private final SelenideElement registerPatientButton = $("button[type='submit']");
    private final SelenideElement patientNameUnknownButton =
            $(byText("Patient's Name is Known?")).parent().parent().$$("button").findBy(exactText("No"));

    private final SelenideElement birthDateUnknownButton =
            $(byText("Date of Birth Known?")).parent().parent().$$("button").findBy(exactText("No"));

    private final SelenideElement genderError = $(byText("Gender is required"));
    private final SelenideElement birthDateError = $("[data-testid='birthdate'] [slot='errorMessage']");

    @Override
    public String url() {
        return UiPath.PATIENT_REGISTRATION;
    }

    @Step("Проверяем, что открыта страница регистрации пациента")
    public PatientRegistrationPage checkPatientRegistrationOpened() {
        givenNameField.shouldBe(visible);
        return this;
    }

    @Step("Вводим имя пациента: {givenName}")
    public PatientRegistrationPage inputGivenName(String givenName) {
        givenNameField.setValue(givenName);
        return this;
    }

    @Step("Вводим фамилию пациента: {familyName}")
    public PatientRegistrationPage inputFamilyName(String familyName) {
        familyNameField.setValue(familyName);
        return this;
    }

    @Step("Выбираем пол пациента: {gender}")
    public PatientRegistrationPage selectGender(String gender) {
        switch (gender) {
            case "M" -> maleGender.click();
            case "F" -> femaleGender.click();
            default -> throw new IllegalArgumentException("Unsupported gender: " + gender);
        }

        return this;
    }

    @Step("Указываем дату рождения: {birthDate}")
    public PatientRegistrationPage inputBirthDate(String birthDate) {
        birthDatePicker.setDate(LocalDate.parse(birthDate));
        return this;
    }

    @Step("Регистрируем пациента")
    public PatientChartPage registerPatient() {
        registerPatientButton.click();

        return getPage(PatientChartPage.class).checkPatientChartOpened();
    }

    @Step("Отправляем форму регистрации пациента")
    public PatientRegistrationPage submitRegistration() {
        registerPatientButton.click();
        return this;
    }

    @Step("Проверяем ошибку обязательного пола")
    public PatientRegistrationPage checkGenderRequiredError() {
        genderError.shouldHave(exactText("Gender is required"), visible);
        return this;
    }

    @Step("Проверяем ошибку обязательной даты рождения")
    public PatientRegistrationPage checkBirthDateRequiredError() {
        birthDateError.shouldHave(exactText("Birthday is required"), visible);
        return this;
    }

    @Step("Указываем, что имя пациента неизвестно")
    public PatientRegistrationPage selectUnknownName() {
        patientNameUnknownButton.click();
        return this;
    }

    @Step("Указываем, что точная дата рождения неизвестна")
    public PatientRegistrationPage selectBirthDateUnknown() {
        birthDateUnknownButton.click();
        return this;
    }

    @Step("Указываем приблизительный возраст")
    public PatientRegistrationPage inputEstimatedAge(String birthDate) {
        LocalDate date = LocalDate.parse(birthDate);
        Period age = Period.between(date, LocalDate.now());

        estimatedYearsField.setValue(String.valueOf(age.getYears()));
        estimatedMonthsField.setValue(String.valueOf(age.getMonths()));
        return this;
    }
}
