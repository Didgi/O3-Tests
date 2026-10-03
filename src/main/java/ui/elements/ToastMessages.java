package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public class ToastMessages extends BaseElement {
    public ToastMessages() {
        super($(".omrs-snackbars-container"));
    }

    private final ElementsCollection messages =
            findAll("[role='alertdialog']");

    public ToastMessages shouldShowOrderUpdated(String drugName) {
        SelenideElement snackbar = messages
                .findBy(text("Order updated"))
                .shouldBe(visible, Duration.ofSeconds(5));

        snackbar
                .$(".cds--actionable-notification__title")
                .shouldHave(exactText("Order updated"));

        snackbar
                .$(".cds--actionable-notification__subtitle")
                .shouldHave(exactText("Updated " + drugName + "."));

        snackbar.shouldHave(
                cssClass("cds--actionable-notification--success"));

        return this;
    }
}
