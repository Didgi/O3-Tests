package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.Getter;
import ui.elements.CreateAppointmentPanel;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AppointmentPage extends BasePage<AppointmentPage> {
    private final SelenideElement getHeaderText = $(Selectors.byTagAndText("p", "Appointments"));
    private final SelenideElement createNewAppointmentButton = $(Selectors.byTagAndText("button", "Create new appointment"));

    @Override
    public String url() {
        return UiPath.APPOINTMENTS;
    }

    @Step("Открываем панель создания appointment")
    public CreateAppointmentPanel openCreateAppointmentPanel() {
        createNewAppointmentButton.click();
        return new CreateAppointmentPanel();
    }
}
