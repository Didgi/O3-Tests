package ui.elements;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selectors.byText;

public class EncounterDetailsElement extends BaseElement {

    private static final By DELETE_ENCOUNTER_BUTTON =
            byText("Delete this encounter");

    public EncounterDetailsElement(SelenideElement element) {
        super(element);
    }

    public DeleteEncounterModal openDeleteModal() {
        find(DELETE_ENCOUNTER_BUTTON).click();

        return new DeleteEncounterModal();
    }
}
