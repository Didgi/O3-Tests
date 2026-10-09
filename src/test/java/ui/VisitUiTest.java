package ui;

import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateResponse;
import api.models.visit.VisitSearchResponse;
import api.testdata.ReferenceTestData;
import common.annotations.WithPatient;
import common.annotations.WithVisit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.PatientChartPage;
import ui.pages.VisitPage;

import static com.codeborne.selenide.Condition.visible;

public class VisitUiTest extends UIBaseTest {
    @Test
    @DisplayName("Переход на страницу visits")
    @WithPatient()
    public void openVisitPageUiTest(PatientResponse patient) {
        new VisitPage(patient.uuid()).open().getVisitTab().shouldBe(visible);
    }

    @Test
    @DisplayName("Создание Visit для пациента")
    @WithPatient
    void createVisitUiTest(PatientResponse patient) {
        new PatientChartPage(patient.uuid())
                .open()
                .openCreateVisitPanel()
                .shouldBeOpened()
                .selectVisitType(ReferenceTestData.visitTypeName())
                .stratVisit()
                .visitStartedShouldAppear()
                .checkActiveVisitBage();

        VisitSearchResponse activeVisits =
                admin.visits().searchVisitsByPatient(patient.uuid());

        softly.assertThat(activeVisits.results())
                .as("Patient has an active visit")
                .isNotEmpty()
                .extracting(VisitCreateResponse::display)
                .anyMatch(display -> display.contains(ReferenceTestData.visitTypeName()));
    }

    @Test
    @DisplayName("Создание Visit невозможно без выбранного типа визита")
    @WithPatient
    void fieldValidationCreateVisitUiTest(PatientResponse patient) {
        new PatientChartPage(patient.uuid())
                .open()
                .openCreateVisitPanel()
                .shouldBeOpened()
                .stratVisit()
                .missingVisitTypeShouldAppear();

        VisitSearchResponse activeVisits =
                admin.visits().searchVisitsByPatient(patient.uuid());

        softly.assertThat(activeVisits.results())
                .as("Patient has an not active visit")
                .isEmpty();
    }

    @Test
    @DisplayName("Создание Visit невозможно если он уже начат")
    @WithPatient
    @WithVisit
    void canNotCreateVisitUiTest(PatientResponse patient) {
        new PatientChartPage(patient.uuid())
                .open()
                .openCreateVisitPanel()
                .shouldBeOpened()
                .patientHasActiveVisitShouldAppear()
                .stratVisitShouldBeDisabled();
    }
}
