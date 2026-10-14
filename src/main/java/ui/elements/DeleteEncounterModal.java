package ui.elements;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class DeleteEncounterModal extends BaseElement {

    private static final By MODAL =
            byAttribute("role", "dialog");

    private static final By DELETE_BUTTON =
            byText("Delete");

    private static final By CANCEL_BUTTON =
            byText("Cancel");

    public DeleteEncounterModal() {
        super($(MODAL));
    }

    public void confirmDelete() {
        find(DELETE_BUTTON).click();
        $(MODAL).should(disappear);
    }

    public void cancel() {
        find(CANCEL_BUTTON).click();
        $(MODAL).should(disappear);
    }
}
