package ui.elements;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import ui.models.EncounterType;

import static com.codeborne.selenide.Condition.exactText;

public class EncountersTableElement extends BaseElement {

    private static final By ENCOUNTER_TYPE_CELL =
            By.cssSelector("tr[data-parent-row='true'] td:nth-child(4)");

    public EncountersTableElement(SelenideElement element) {
        super(element);
    }

    public EncounterRowElement getEncounter(EncounterType encounterType) {
        SelenideElement encounterTypeCell = findAll(ENCOUNTER_TYPE_CELL)
                .findBy(exactText(encounterType.getDisplayName()));

        return new EncounterRowElement(
                encounterTypeCell.closest("tr")
        );
    }
}
