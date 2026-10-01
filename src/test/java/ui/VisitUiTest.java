package ui;

import api.models.patients.PatientResponse;
import common.annotations.WithPatient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.ServiceQueuesPage;
import ui.pages.VisitPage;

import static com.codeborne.selenide.Condition.visible;

public class VisitUiTest extends UIBaseTest {
    @Test
    @DisplayName("Переход на страницу visits")
    @WithPatient()
    public void openVisitPageUiTest(PatientResponse patient) {
        new VisitPage(patient.uuid()).open().getVisitTab().shouldBe(visible);
    }
}
