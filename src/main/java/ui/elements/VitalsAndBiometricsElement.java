package ui.elements;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import ui.models.VitalType;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;

public class VitalsAndBiometricsElement extends BaseElement {

    private static final By RECORD_VITALS_BUTTON =
            byText("Record vitals");

    private static final By OBSERVATION_CARD =
            byAttribute("data-testid", "numeric-observation-card");

    private static final String OBSERVATION_VALUE =
            "[id^='omrs-numeric-obs-value-']";

    public VitalsAndBiometricsElement(SelenideElement element) {
        super(element);
    }

    public RecordVitalsAndBiometricsForm openRecordVitals() {
        find(RECORD_VITALS_BUTTON).click();

        return new RecordVitalsAndBiometricsForm();
    }

    public String getObservationValue(VitalType vitalType) {
        return getObservationCard(vitalType)
                .find(OBSERVATION_VALUE)
                .getText();
    }

    private SelenideElement getObservationCard(VitalType vitalType) {
        return findAll(OBSERVATION_CARD)
                .findBy(text(vitalType.getDisplayName()));
    }
}
