package ui.elements;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.attribute;

public class EncounterRowElement extends BaseElement {

    private static final By ROW_TOGGLE_BUTTON =
            By.cssSelector("button[aria-controls]");

    public EncounterRowElement(SelenideElement element) {
        super(element);
    }

    public EncounterDetailsElement expand() {
        SelenideElement toggleButton = find(ROW_TOGGLE_BUTTON);

        if (!"true".equals(toggleButton.getAttribute("aria-expanded"))) {
            toggleButton.click();
        }

        SelenideElement detailsRow = toggleButton
                .closest("tr")
                .sibling(0)
                .shouldHave(attribute("data-child-row", "true"));

        return new EncounterDetailsElement(detailsRow);
    }
}
