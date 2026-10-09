package ui.elements;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import ui.pages.PatientChartPage;

import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byTagAndText;
import static com.codeborne.selenide.Selenide.$;

public class CreateVisitPanel extends BaseElement {
    private final static SelenideElement createVisitPanel = $("#omrs-workspaces-container");
    private final SelenideElement startVisitHeader = createVisitPanel.$(byTagAndText("span", "Start a visit"));
    private final SelenideElement startVisitButton = createVisitPanel.$(byAttribute("type", "submit"));
    private final SelenideElement visitStartedPopup = $(Selectors.byText("Visit started"));
    private final SelenideElement missingVisitTypeMessage = createVisitPanel.$(Selectors.byText("Missing visit type"));
    private final SelenideElement selectAVisitTypeMessage = createVisitPanel.$(Selectors.byText("Please select a visit type"));
    private final SelenideElement patientAlreadyHasAnActiveVisitMessage = createVisitPanel.$(Selectors.byText("This patient already has an active visit"));

    public CreateVisitPanel() {
        super(createVisitPanel);
    }

    public CreateVisitPanel shouldBeOpened() {
        startVisitHeader.shouldBe(visible);
        return this;
    }

    public CreateVisitPanel stratVisit() {
        startVisitButton.click();
        return this;
    }

    public CreateVisitPanel selectVisitType(String visitType) {
        createVisitPanel.$(byTagAndText("span", visitType)).click();
        return this;
    }

    public PatientChartPage visitStartedShouldAppear() {
        visitStartedPopup.shouldBe(visible);
        return new PatientChartPage();
    }

    public PatientChartPage missingVisitTypeShouldAppear() {
        missingVisitTypeMessage.shouldBe(visible);
        selectAVisitTypeMessage.shouldBe(visible);
        return new PatientChartPage();
    }

    public CreateVisitPanel patientHasActiveVisitShouldAppear() {
        patientAlreadyHasAnActiveVisitMessage.shouldBe(visible);
        return this;
    }

    public CreateVisitPanel stratVisitShouldBeDisabled() {
        startVisitButton.shouldBe(disabled);
        return this;
    }
}
