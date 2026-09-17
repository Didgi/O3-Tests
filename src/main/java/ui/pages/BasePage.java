package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import common.StepLogger;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

@Getter
@NoArgsConstructor
public abstract class BasePage<T extends BasePage> {

    public abstract String url();


    public T open() {
        return StepLogger.log("Открываем сайт", () -> Selenide.open(url(), (Class<T>) this.getClass()));
    }

    public <T extends BasePage> T getPage(Class<T> classPage) {

        return StepLogger.log("Переходим на страницу", () -> Selenide.page(classPage));
    }

    public <T extends BaseElement> List<T> generateElementList(ElementsCollection elements,
                                                               Function<SelenideElement, T> constructor) {
        return elements.stream().map(constructor).toList();
    }

    @Step("Обновляем страницу")
    public T refreshPage() {
        Selenide.refresh();
        return (T) this;
    }
}
