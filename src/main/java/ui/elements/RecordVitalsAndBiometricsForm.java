package ui.elements;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.hidden;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byName;
import static com.codeborne.selenide.Selenide.$;

public class RecordVitalsAndBiometricsForm extends BaseElement {

    private final SelenideElement weightInput =
            find(byName("Weight"));

    private final SelenideElement heightInput =
            find(byName("Height"));

    private final SelenideElement saveButton =
            find(byAttribute("type", "submit"));

    public RecordVitalsAndBiometricsForm() {
        super($("[data-openmrs-role='Vitals and Biometrics Form']"));
    }

    public PatientAsideElement recordWeightAndHeight(
            String weight,
            String height
    ) {
        weightInput.setValue(weight);
        heightInput.setValue(height);
        saveButton.click();

        weightInput.shouldBe(hidden);

        return new PatientAsideElement();
    }
}
