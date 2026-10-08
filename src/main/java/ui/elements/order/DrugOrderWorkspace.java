package ui.elements.order;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ui.elements.BaseElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byTagAndText;
import static com.codeborne.selenide.Selenide.$;

public class DrugOrderWorkspace extends BaseElement {

    public DrugOrderWorkspace() {
        super($("[data-extension-slot-name='allergy-list-pills-slot']").parent());
    }

    private final SelenideElement drugSearchInput =
            find(
                    "[role='search'][aria-label^='Search for a drug or orderset'] " +
                            "input[type='search']"
            );
    private final SelenideElement searchResultContainer =
            find("[class*='__order-basket-search-results__resultsContainer___']");
    private final ElementsCollection searchResults =
            searchResultContainer.$$("[role='listitem']");

    public SelenideElement findDrug(String drugName) {
        drugSearchInput.setValue(drugName);
        return searchResults.findBy(text(drugName));
    }

    public DrugOrderForm<OrderBasketWorkspace> addDrugToOrderForm(
            String drugName
    ) {
        findDrug(drugName)
                .find(byTagAndText("button", "Order form"))
                .click();
        return new DrugOrderForm<>(OrderBasketWorkspace::new);
    }
}
