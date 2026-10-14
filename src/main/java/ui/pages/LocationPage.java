package ui.pages;

import com.codeborne.selenide.*;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Getter
@NoArgsConstructor
public class LocationPage extends BasePage<LocationPage> {

    private final SelenideElement searchField = $("[placeholder='Search for a location']");
    private final ElementsCollection locations = $$(".cds--radio-button__label");
    private final SelenideElement checkboxRememberLocation = $(".cds--checkbox");
    private final SelenideElement confirmButton = $$("button[type='submit']").find(Condition.exactText("Confirm"));

    @Override
    public String url() {
        return UiPath.LOGIN;
    }

    @Step("Проверяем, что открыта страница выбора локации")
    public LocationPage checkLocationPageChooseOpened() {
        searchField.shouldBe(visible);
        locations.get(0).shouldBe(visible);
        return this;
    }

    @Step("Вводим значение в поле поиска локации")
    public LocationPage inputLocationName(String value) {
        searchField.setValue(value);
        return this;
    }

    @Step("Проверяем количество найденных локаций")
    public LocationPage checkLocationAmount(int amount) {
        locations.shouldHave(CollectionCondition.size(amount));
        return this;
    }

    @Step("Проверяем найденную локацию")
    public LocationPage checkFoundLocation(String location) {
        locations.shouldHave(CollectionCondition.itemWithText(location));
        return this;
    }

    @Step("Выделяем локацию")
    public LocationPage selectLocation(String location) {
        SelenideElement chooseLocation = locations
                .findBy(Condition.exactText(location))
                .shouldBe(Condition.visible);

        chooseLocation.click();

        String radioId = chooseLocation.getAttribute("for");

        $("[id='" + radioId + "']")
                .shouldBe(Condition.selected);
        return this;
    }


    @Step("Проверяем состояние чекбокса")
    public LocationPage checkCheckboxState(boolean value) {
        checkboxRememberLocation.should(visible);
        final boolean actualState = checkboxRememberLocation.isSelected();
        Assertions.assertThat(actualState).isEqualTo(value);
        return this;
    }

    @Step("Кликаем по чекбоксу")
    public LocationPage clickCheckbox() {
        checkboxRememberLocation.click();
        return this;
    }

    @Step("Проверяем, что кнопка Confirm не активна")
    public LocationPage checkConfirmButtonNotClickable() {
        confirmButton.shouldBe(disabled);
        return this;
    }

    @Step("Проверяем, что кнопка Confirm активна")
    public LocationPage checkConfirmButtonClickable() {
        confirmButton.shouldNotBe(disabled);
        return this;
    }

    @Step("Кликаем по кнопке Confirm")
    public LocationPage clickConfirmButton() {
        confirmButton.click();
        return this;
    }
}
