package ui.elements;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import java.time.LocalDate;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class CreateAppointmentPanel extends BaseElement {
    private final SelenideElement searchInput = find("[data-testid='patientSearchBar']");
    private final SelenideElement serviceInput = $(Selectors.byId("service"));
    private final DatePicker datePicker = new DatePicker("datePickerInput");
    private final SelenideElement durationField = find("#duration");
    private final SelenideElement timeField = find("#time-picker");
    private final SelenideElement appointmentNoteField = find("#appointmentNote");
    private final SelenideElement saveAndCloseButton = $(Selectors.byTagAndText("button", "Save and close"));
    private final SelenideElement selectFormatTime = $("#time-picker-select-1");
    private final static SelenideElement createAppointmentPanel = $("#omrs-workspaces-container");



    public CreateAppointmentPanel() {
        super(createAppointmentPanel);
    }

    public CreateAppointmentPanel shouldBeOpened() {
        find(Selectors.byText("Create new appointment")).shouldBe(visible);
        searchInput.shouldBe(visible);
        return this;
    }

    public CreateAppointmentPanel searchPatient(String query) {
        searchInput.setValue(query);
        find(Selectors.byTagAndText("button", "Search")).click();
        return this;
    }

    public CreateAppointmentPanel selectPatient(String name) {
        find(Selectors.byTagAndText("span", name)).click();
        return this;
    }

    public CreateAppointmentPanel patientShouldBeFound(String identifier) {
        find(Selectors.withText(identifier)).shouldBe(visible);
        return this;
    }

    public CreateAppointmentPanel selectService(String service) {
        serviceInput.selectOption(service);
        return this;
    }

    public CreateAppointmentPanel selectDate(LocalDate date) {
        datePicker.setDate(date);
        return this;
    }

    public CreateAppointmentPanel enterTime(String time) {
        timeField.click();
        timeField.setValue(time);
        return this;
    }

    public CreateAppointmentPanel selectFormat() {
        selectFormatTime.selectOption("PM");
        return this;
    }

    public CreateAppointmentPanel enterDuration(String duration) {
        durationField.click();
        durationField.setValue(duration);
        return this;
    }

    public CreateAppointmentPanel enterNote(String note) {
        appointmentNoteField.click();
        appointmentNoteField.setValue(note);
        return this;
    }

    public CreateAppointmentPanel saveAndClose() {
        saveAndCloseButton.click();
        return this;
    }

    public CreateAppointmentPanel appointmentScheduledShouldAppear() {
        $(Selectors.byText("Appointment scheduled")).shouldBe(visible);
        return this;
    }

    public CreateAppointmentPanel fillForm(
            String service,
            LocalDate date,
            String time,
            String duration,
            String note
    ) {
        selectService(service);
        selectDate(date);
        enterTime(time);
        enterDuration(duration);
        enterNote(note);
        return this;
    }
}
