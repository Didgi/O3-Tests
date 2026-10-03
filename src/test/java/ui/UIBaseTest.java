package ui;

import api.BaseApiTest;
import api.config.Roles;
import common.annotations.UiCookieAnnotation;
import common.annotations.UiSetupAnnotation;
import common.annotations.WithUser;
import common.extensions.BrowserExtension;
import common.extensions.UiCookieExtension;
import common.extensions.UiSetupExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.pages.LoginPage;
import ui.pages.ServiceQueuesPage;

@WithUser(role = Roles.SUPER_ADMIN)
@UiCookieAnnotation
@UiSetupAnnotation
@ExtendWith({
        BrowserExtension.class,
        UiCookieExtension.class,
        UiSetupExtension.class
})
public class UIBaseTest extends BaseApiTest {

    protected ServiceQueuesPage serviceQueuesPage;
    protected LoginPage loginPage;

    @BeforeEach
    public void setUpUiTests() {
        loginPage = new LoginPage();
        serviceQueuesPage = new ServiceQueuesPage();

    }

}
