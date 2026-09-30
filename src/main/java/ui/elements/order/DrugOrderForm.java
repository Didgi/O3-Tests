package ui.elements.order;

import com.codeborne.selenide.SelenideElement;
import ui.elements.BaseElement;
import ui.models.DrugOrderData;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byTagAndText;
import static com.codeborne.selenide.Selenide.$;

public class DrugOrderForm extends BaseElement {
    public DrugOrderForm() {
        super($("#drugOrderForm"));
    }

    private final SelenideElement medicationInfo = find("#medicationInfo");
    private final SelenideElement doseInput = find("#doseSelection");
    private final SelenideElement dosingUnitCombobox = find("#dosingUnits");
    private final SelenideElement routeCombobox = find("#editRoute");
    private final SelenideElement frequencyCombobox = find("#editFrequency");
    private final SelenideElement quantityDispensedInput = find("#quantityDispensed");
    private final SelenideElement quantityUnitsCombobox = find("#dispensingUnits");
    private final SelenideElement prescriptionRefillsInput = find("#prescriptionRefills");
    private final SelenideElement indicationInput = find("#indication");
    private final SelenideElement submitFormButton = find(byTagAndText("button", "Save order"));

    public DrugOrderForm shouldHaveCorrectDrug(String drugName) {
        medicationInfo.shouldHave(text(drugName));
        return this;
    }

    public DrugOrderForm setDose(String dose) {
        doseInput.setValue(dose);
        return this;
    }

    public DrugOrderForm setDoseUnit(String unit) {
        selectComboboxOption(dosingUnitCombobox, unit);
        return this;
    }

    public DrugOrderForm setRoute(String route) {
        selectComboboxOption(routeCombobox, route);
        return this;
    }

    public DrugOrderForm setFrequency(String frequency) {
        selectComboboxOption(frequencyCombobox, frequency);
        return this;
    }

    public DrugOrderForm setDispenseQuantity(String dispenseQuantity) {
        quantityDispensedInput.setValue(dispenseQuantity);
        return this;
    }

    public DrugOrderForm setQuantityUnits(String quantityUnits) {
        selectComboboxOption(quantityUnitsCombobox, quantityUnits);
        return this;
    }

    public DrugOrderForm setPrescriptionRefills(String prescriptionRefills) {
        prescriptionRefillsInput.setValue(prescriptionRefills);
        return this;
    }

    public DrugOrderForm setIndication(String indication) {
        indicationInput.setValue(indication);
        return this;
    }

    public DrugOrderForm fill(DrugOrderData order) {
        setDose(order.dosage().amount());
        setDoseUnit(order.dosage().unit());
        setRoute(order.dosage().route());
        setFrequency(order.dosage().frequency());
        setDispenseQuantity(order.dispensing().quantity());
        setQuantityUnits(order.dispensing().unit());
        setPrescriptionRefills(order.dispensing().refills());
        setIndication(order.indication());
        return this;
    }

    public OrderBasketWorkspace saveOrder() {
        submitFormButton.click();
        return new OrderBasketWorkspace();
    }

    public OrderBasketWorkspace fillAndSubmit(DrugOrderData order) {
        fill(order);
        return saveOrder();
    }

    private void selectComboboxOption(
            SelenideElement input,
            String optionText
    ) {
        SelenideElement comboBox = input.closest(".cds--combo-box");

        input.shouldBe(visible, enabled).click();

        comboBox.$("[role='listbox']")
                .shouldBe(visible)
                .$$("[role='option']")
                .findBy(exactText(optionText))
                .shouldBe(visible)
                .click();

        input.shouldHave(value(optionText));
    }
}
