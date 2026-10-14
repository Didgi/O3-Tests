package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Getter
@NoArgsConstructor
public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement usernameField = $("#username");
    private final SelenideElement passwordField = $("#password");
    private final SelenideElement continueButton = $$("button[type='submit']").find(Condition.exactText("Continue"));
    private final SelenideElement loginButton = $$("button[type='submit']").find(Condition.exactText("Log in"));
    private final SelenideElement errorTitle = $(".cds--inline-notification__title");
    private final SelenideElement errorDetails = $(".cds--inline-notification__subtitle");
    private final static String expectedErrorTitleText = "Error";
    private final static String expectedErrorDetailsTextText = "Invalid username or password";

    @Override
    public String url() {
        return UiPath.LOGIN;
    }

    @Step("Проверяем, что открыта страница логина")
    public LoginPage checkLoginPageOpened() {
        usernameField.shouldBe(visible);
        continueButton.shouldBe(visible);
        return this;
    }

    @Step("Вводим значение в поле username")
    public LoginPage inputUsername(String value) {
        usernameField.setValue(value);
        return this;
    }

    @Step("Вводим значение в поле password")
    public LoginPage inputPassword(String value) {
        passwordField.setValue(value);
        return this;
    }

    @Step("Кликаем по кнопке Continue")
    public LoginPage clickContinue() {
        continueButton.click();
        return this;
    }

    @Step("Кликаем по кнопке Log in")
    public LoginPage clickLogIn() {
        loginButton.click();
        return this;
    }

    @Step("Проверка отображаемую ошибку при вводе невалидного значения логина/пароля")
    public LoginPage checkLoginError() {
        errorTitle.should(visible);
        errorDetails.should(visible);
        String actualErrorTitle = errorTitle.getText();
        String actualErrorDetails = errorDetails.getText();
        Assertions.assertThat(actualErrorTitle).isEqualTo(expectedErrorTitleText);
        Assertions.assertThat(actualErrorDetails).isEqualTo(expectedErrorDetailsTextText);
        return this;
    }
}
