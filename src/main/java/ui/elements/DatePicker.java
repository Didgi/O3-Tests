package ui.elements;

import com.codeborne.selenide.SelenideElement;

import java.time.LocalDate;

import static com.codeborne.selenide.Selenide.$;

public class DatePicker extends BaseElement {

    private final SelenideElement dayField =
            find("[data-type='day']");

    private final SelenideElement monthField =
            find("[data-type='month']");

    private final SelenideElement yearField =
            find("[data-type='year']");

    public DatePicker(String testId) {
        super($("[data-testid='" + testId + "']"));
    }

    public DatePicker setDate(LocalDate date) {
        dayField.click();
        dayField.sendKeys(String.valueOf(date.getDayOfMonth()));

        monthField.click();
        monthField.sendKeys(String.valueOf(date.getMonthValue()));

        yearField.click();
        yearField.sendKeys(String.valueOf(date.getYear()));

        return this;
    }
}