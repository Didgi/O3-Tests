package ui;

import api.models.observations.ObservationCreateRequest;
import api.models.observations.ObservationResponse;
import api.models.patients.PatientResponse;
import api.testdata.SeedObservationConcept;
import api.testdata.VisitTestData;
import common.annotations.GeneratedObservationRequest;
import common.annotations.WithPatient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.elements.PatientAsideElement;
import ui.pages.PatientChartPage;

public class VitalsAndBiometricsTest extends UIBaseTest {
    private PatientAsideElement patientAside;

    @BeforeEach
    void setUp() {
        patientAside = new PatientAsideElement();
    }

    @Test
    @WithPatient
    @DisplayName("Сохранение веса и роста пациента через UI")
    void saveWeightAndHeight(
            PatientResponse patient,
            @GeneratedObservationRequest ObservationCreateRequest weightRequest,
            @GeneratedObservationRequest(
                    concept = SeedObservationConcept.HEIGHT
            ) ObservationCreateRequest heightRequest
    ) {
        admin.visits().createVisit(
                VisitTestData.activeVisitCreateRequest(patient.uuid())
        );

        new PatientChartPage(patient.uuid()).open();

        patientAside
                .getVitalsAndBiometrics()
                .openRecordVitals()
                .recordWeightAndHeight(
                        weightRequest.value().asText(),
                        heightRequest.value().asText()
                );

        ObservationResponse actualWeight =
                admin.observations().getObservationByPatientAndConcept(
                        patient.uuid(),
                        weightRequest.concept()
                );

        ObservationResponse actualHeight =
                admin.observations().getObservationByPatientAndConcept(
                        patient.uuid(),
                        heightRequest.concept()
                );

        softly.assertThat(actualWeight.value().decimalValue())
                .isEqualByComparingTo(
                        weightRequest.value().decimalValue()
                );

        softly.assertThat(actualHeight.value().decimalValue())
                .isEqualByComparingTo(
                        heightRequest.value().decimalValue()
                );
    }
}
