package ui;

import api.config.Config;
import common.annotations.UiCookieAnnotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.LoginPage;
import ui.pages.ServiceQueuesPage;

public class UIAuthTest extends UIBaseTest {

    @Test
    @DisplayName("Позитивный тест: админ может авторизоваться с валидными данными")
    public void adminCanLoginWithValidData() {

        loginPage
                .open()
                .inputUsername(Config.getProperty("ADMIN_USERNAME"))
                .clickContinue()
                .inputPassword("ADMIN_PASSWORD")
                .clickLogIn()
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
                .checkLoginPageOpened();
    }

    @Test
    @DisplayName("Позитивный тест: пользователь может авторизоваться с валидными данными")
    public void userCanChangeHisNameWithValidData2() {

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened();
    }
}
