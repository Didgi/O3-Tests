package ui;

import api.config.Config;
import common.Locations;
import common.annotations.UiCookieAnnotation;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.LocationPage;
import ui.pages.LoginPage;
import ui.pages.ServiceQueuesPage;

import java.util.stream.Stream;

public class UIAuthTest extends UIBaseTest {

    @Test
    @UiCookieAnnotation(enabled = false)
    @DisplayName("Позитивный тест: админ может авторизоваться с валидными данными без сохранения локации")
    public void adminCanLoginWithValidDataWithoutSaveChosenLocation() {

        final String shortLocationNameToSearch = "Inpatient";
        final int expectedAmountFoundLocations = 1;

        loginPage
                .open()
                .inputUsername(Config.getProperty("ADMIN_USERNAME"))
                .clickContinue()
                .inputPassword(Config.getProperty("ADMIN_PASSWORD"))
                .clickLogIn()
                .getPage(LocationPage.class)
                .checkLocationPageChooseOpened()
                .checkConfirmButtonNotClickable()
                .inputLocationName(shortLocationNameToSearch)
                .checkLocationAmount(expectedAmountFoundLocations)
                .checkFoundLocation(Locations.INPATIENT_WARD.getValue())
                .selectLocation(Locations.INPATIENT_WARD.getValue())
                .checkConfirmButtonClickable()
                .checkCheckboxState(false)
                .clickConfirmButton()
                .getPage(ServiceQueuesPage.class)
                .checkServiceQueuesOpened();
    }

    @Test
    @DisplayName("Позитивный тест: админ может выполнить логаут")
    public void adminCanLogoutWithValidData() {

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .openSettings()
                .clickLogout()
                .getPage(LoginPage.class)
                .checkLoginPageOpened()
                .getPage(ServiceQueuesPage.class)
                .open()
                .getPage(LoginPage.class)
                .checkLoginPageOpened();
    }

    private static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of(new Faker().name().firstName(), Config.getProperty("ADMIN_PASSWORD")),
                Arguments.of(Config.getProperty("ADMIN_USERNAME"), new Faker().name().lastName()),
                Arguments.of(new Faker().name().firstName(), new Faker().name().lastName())
        );
    }

    @MethodSource("invalidCredentials")
    @ParameterizedTest
    @UiCookieAnnotation(enabled = false)
    @DisplayName("Негативный тест: админ не может авторизоваться с не валидными данными")
    public void adminCannotLoginWithInvalidData(String username, String password) {

        loginPage
                .open()
                .inputUsername(username)
                .clickContinue()
                .inputPassword(password)
                .clickLogIn()
                .checkLoginPageOpened()
                .checkLoginError();
    }
}
