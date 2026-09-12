package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

public abstract class BaseElement {
    private final SelenideElement element;

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
}
