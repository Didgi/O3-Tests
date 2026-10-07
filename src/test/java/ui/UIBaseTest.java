package ui;

import api.BaseApiTest;
import api.config.Config;
import api.config.Roles;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import common.annotations.UiCookieAnnotation;
import common.annotations.WithUser;
import common.extensions.BrowserExtension;
import common.extensions.UiCookieExtension;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.pages.LoginPage;
import ui.pages.ServiceQueuesPage;

import java.util.Map;

@WithUser(role = Roles.SUPER_ADMIN)
@UiCookieAnnotation
@ExtendWith({
        BrowserExtension.class,
        UiCookieExtension.class
})
public class UIBaseTest extends BaseApiTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("remote_host");
        Configuration.baseUrl = Config.getProperty("ui_baseurl");
        Configuration.browser = Config.getProperty("browsers");
        Configuration.browserSize = Config.getProperty("resolution");
        Configuration.timeout = 10_000;
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", Boolean.parseBoolean(Config.getProperty("enable_vnc")),
                        "enableLog", Boolean.parseBoolean(Config.getProperty("enable_log")),
                        "enableVideo", Boolean.parseBoolean(Config.getProperty("enable_recording_video")))
        );

        Configuration.headless = Boolean.parseBoolean(Config.getProperty("headless_mode"));
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));
    }

    protected ServiceQueuesPage serviceQueuesPage;
    protected LoginPage loginPage;

    @BeforeEach
    public void setUpUiTests() {
        loginPage = new LoginPage();
        serviceQueuesPage = new ServiceQueuesPage();

    }

    @AfterEach
    @Step("Завершаем тесты. Закрываем WebDriver")
    public void tearDownUiTests() {
        Selenide.closeWebDriver();
    }
}
