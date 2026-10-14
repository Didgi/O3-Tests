package ui.elements;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;

public class PatientEditForm extends BaseElement {

    private final SelenideElement givenNameField =
            find("#givenName");

    private final SelenideElement updatePatientButton =
            find("button[type='submit']");

    private final SelenideElement cancelButton =
            find(byText("Cancel"));

    public PatientEditForm(SelenideElement element) {
        super(element);
    }

    public PatientEditForm checkPatientEditFormOpened() {
        element.shouldBe(visible);
        givenNameField.shouldBe(visible);
        return this;
    }

    public PatientEditForm inputGivenName(String givenName) {
        replaceInputValue(givenNameField, givenName);
        return this;
    }


    public void submit() {
        updatePatientButton.click();
    }

    public void cancel() {
        cancelButton.click();
    }
}