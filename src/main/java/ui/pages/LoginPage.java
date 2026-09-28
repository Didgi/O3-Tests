package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Getter
@NoArgsConstructor
public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement usernameField = $("#username");
    private final SelenideElement passwordField = $("#password");
    private final SelenideElement continueButton = $(Selectors.byText("Continue"));
    private final SelenideElement loginButton = $(Selectors.byText("Log in"));

    @Override
    public String url() {
        return UiPath.LOGIN;
    }

    @Step("Проверяем, что открыта страница логина")
    public LoginPage checkLoginPageOpened(){
        usernameField.shouldBe(visible);
        continueButton.shouldBe(visible);
        return this;
    }

    @Step("Вводим значение в поле username")
    public LoginPage inputUsername(String value){
        usernameField.setValue(value);
        return this;
    }

    @Step("Вводим значение в поле password")
    public LoginPage inputPassword(String value){
        passwordField.setValue(value);
        return this;
    }

    @Step("Кликаем по кнопке Continue")
    public LoginPage clickContinue(){
        continueButton.click();
        return this;
    }

    @Step("Кликаем по кнопке Log in")
    public LoginPage clickLogIn(){
        loginButton.click();
        return this;
    }
}
