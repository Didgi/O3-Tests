package ui.elements.order;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;
import ui.elements.BaseElement;
import ui.models.DrugOrderData;

import java.util.function.Supplier;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byTagAndText;
import static com.codeborne.selenide.Selenide.$;

public class DrugOrderForm<NEXT> extends BaseElement {
    private final Supplier<NEXT> nextAfterSave;

    public DrugOrderForm(Supplier<NEXT> nextAfterSave) {
        super($("#drugOrderForm"));
        this.nextAfterSave = nextAfterSave;
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

    public DrugOrderForm<NEXT> shouldHaveCorrectDrug(String drugName) {
        medicationInfo.shouldHave(text(drugName));
        return this;
    }

    public DrugOrderForm<NEXT> setDose(String dose) {
        replaceInputValue(doseInput, dose);
        return this;
    }

    public DrugOrderForm<NEXT> setDoseUnit(String unit) {
        selectComboboxOption(dosingUnitCombobox, unit);
        return this;
    }

    public DrugOrderForm<NEXT> setRoute(String route) {
        selectComboboxOption(routeCombobox, route);
        return this;
    }

    public DrugOrderForm<NEXT> setFrequency(String frequency) {
        selectComboboxOption(frequencyCombobox, frequency);
        return this;
    }

    public DrugOrderForm<NEXT> setDispenseQuantity(String dispenseQuantity) {
        replaceInputValue(quantityDispensedInput, dispenseQuantity);
        return this;
    }

    public DrugOrderForm<NEXT> setQuantityUnits(String quantityUnits) {
        selectComboboxOption(quantityUnitsCombobox, quantityUnits);
        return this;
    }

    public DrugOrderForm<NEXT> setPrescriptionRefills(String prescriptionRefills) {
        replaceInputValue(prescriptionRefillsInput, prescriptionRefills);
        return this;
    }

    public DrugOrderForm<NEXT> setIndication(String indication) {
        indicationInput.setValue(indication);
        return this;
    }

    public DrugOrderForm<NEXT> fill(DrugOrderData order) {
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

    public NEXT saveOrder() {
        submitFormButton.click();
        element.should(disappear);

        return nextAfterSave.get();
    }

    public NEXT fillAndSubmit(DrugOrderData order) {
        fill(order);
        return saveOrder();
    }

    private void replaceInputValue(
            SelenideElement input,
            String newValue
    ) {
        input.shouldBe(visible, enabled).click();
        String currentValue = input.getValue();

        input.press(Keys.END);
        for (int i = 0; i < currentValue.length(); i++) {
            input.press(Keys.BACK_SPACE);
        }

        input.shouldHave(exactValue(""))
                .press(newValue)
                .shouldHave(exactValue(newValue));
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
