package ui;

import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterResponse;
import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateResponse;
import api.testdata.EncounterTestData;
import api.testdata.ReferenceTestData;
import common.annotations.WithEncounter;
import common.retry.RetryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.elements.VisitDetailOverviewElement;
import ui.models.EncounterType;
import ui.pages.VisitPage;

import static com.codeborne.selenide.Selenide.open;

public class EncounterUiTest extends UIBaseTest {
    private VisitDetailOverviewElement visitDetailOverview;

    @BeforeEach
    void setUp() {
        visitDetailOverview = new VisitDetailOverviewElement();
    }

    @Test
    @WithEncounter
    @DisplayName("Удаление Encounter через UI")
    void deleteEncounter(
            PatientResponse patient,
            EncounterResponse encounter
    ) {
        new VisitPage(patient.uuid()).open();

        visitDetailOverview
                .openAllEncounters()
                .getEncountersTable()
                .getEncounter(EncounterType.CONSULTATION)
                .expand()
                .openDeleteModal()
                .confirmDelete();

        EncounterResponse actualEncounter = RetryUtils.retry(
                () -> admin.encounters().getEncounterFull(encounter.uuid()),
                EncounterResponse::voided
        );

        softly.assertThat(actualEncounter.voided()).isTrue();
    }

    @Test
    @WithEncounter
    @DisplayName("Отмена удаления Encounter")
    void cancelEncounterDeletion(
            PatientResponse patient,
            EncounterResponse encounter
    ) {
        new VisitPage(patient.uuid()).open();

        visitDetailOverview
                .openAllEncounters()
                .getEncountersTable()
                .getEncounter(EncounterType.CONSULTATION)
                .expand()
                .openDeleteModal()
                .cancel();

        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(encounter.uuid());

        softly.assertThat(actualEncounter.voided()).isFalse();
    }

    @Test
    @WithEncounter
    @DisplayName("Удаляется только выбранный Encounter")
    void deleteOnlySelectedEncounter(
            PatientResponse patient,
            VisitCreateResponse visit,
            EncounterResponse consultationEncounter
    ) {
        EncounterCreateRequest vitalsRequest =
                EncounterTestData.withEncounterType(
                        EncounterTestData.validEncounter(
                                patient.uuid(),
                                visit.uuid()
                        ),
                        ReferenceTestData.encounterVitalsTypeUuid()
                );

        EncounterResponse vitalsEncounter =
                admin.encounters().createEncounter(vitalsRequest);

        new VisitPage(patient.uuid()).open();

        visitDetailOverview
                .openAllEncounters()
                .getEncountersTable()
                .getEncounter(EncounterType.CONSULTATION)
                .expand()
                .openDeleteModal()
                .confirmDelete();

        EncounterResponse deletedConsultation =
                RetryUtils.retry(
                        () -> admin.encounters()
                                .getEncounterFull(consultationEncounter.uuid()),
                        EncounterResponse::voided
                );

        EncounterResponse actualVitals =
                admin.encounters()
                        .getEncounterFull(vitalsEncounter.uuid());

        softly.assertThat(deletedConsultation.voided())
                .isTrue();

        softly.assertThat(actualVitals.voided())
                .isFalse();
    }
}
