package ui.elements;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;

public class VisitDetailOverviewElement extends BaseElement {

    private static final By TAB =
            byAttribute("role", "tab");

    private static final By TAB_PANEL =
            byAttribute("role", "tabpanel");

    public VisitDetailOverviewElement() {
        super($("[data-extension-id='past-visits-detail-overview']"));
    }

    public VisitDetailOverviewElement openAllEncounters() {
        findAll(TAB)
                .findBy(exactText("All encounters"))
                .click();

        return this;
    }

    public EncountersTableElement getEncountersTable() {
        return new EncountersTableElement(
                getActiveTabPanel().find("table")
        );
    }

    private SelenideElement getActiveTabPanel() {
        return findAll(TAB_PANEL)
                .findBy(visible);
    }
}
