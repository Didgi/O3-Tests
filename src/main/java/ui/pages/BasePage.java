package ui.pages;

import api.config.Config;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import common.StepLogger;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openqa.selenium.Cookie;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static ui.pages.UiPath.COOKIE_PATH;

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

    @Step("Сохраняем сессию пользователя в cookie")
    public static void putSessionIntoCookie(String session) {
        Selenide.open("/");

        Cookie cookie = new Cookie.Builder(
                Config.getProperty("cookie_session_name"),
                session
        ).path(COOKIE_PATH).build();

        WebDriverRunner.getWebDriver()
                .manage()
                .addCookie(cookie);
    }
}
