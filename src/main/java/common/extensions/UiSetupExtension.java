package common.extensions;

import api.config.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import common.annotations.UiSetupAnnotation;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Map;

public class UiSetupExtension implements BeforeAllCallback, AfterEachCallback {

    @Override
    public void beforeAll(
            ExtensionContext context
    ) throws Exception {

        if (!hasSelenoidAnnotation(context)) {
            return;
        }

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


        if (Boolean.parseBoolean(Config.getProperty("remote_run"))) {
            Configuration.remote = Config.getProperty("remote_host");
            Configuration.baseUrl = Config.getProperty("ui_remote_baseurl");

        } else {
            Configuration.baseUrl = Config.getProperty("ui_local_baseurl");
        }
    }

    private boolean hasSelenoidAnnotation(ExtensionContext context) {

        return
                AnnotationSupport.isAnnotated(
                        context.getRequiredTestClass(),
                        UiSetupAnnotation.class
                );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Selenide.closeWebDriver();
    }
}
