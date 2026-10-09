package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Condition.exactValue;

public abstract class BaseElement {
    protected final SelenideElement element;

    public BaseElement(SelenideElement element) {
        this.element = element;
    }

    public SelenideElement find(By selector){
        return element.find(selector);
    }

    public SelenideElement find(String cssSelector){
        return element.find(cssSelector);
    }

    public ElementsCollection findAll(By selector){
        return element.findAll(selector);
    }

    public ElementsCollection findAll(String cssSelector){
        return element.findAll(cssSelector);
    }

    protected void replaceInputValue(SelenideElement input, String newValue) {
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
}
