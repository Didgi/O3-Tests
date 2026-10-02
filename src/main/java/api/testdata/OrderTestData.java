package api.testdata;

import api.models.encounter.EncounterResponse;
import api.models.order.CareSetting;
import api.models.order.DrugOrderCreateRequest;
import api.models.order.OrderAction;
import api.models.order.OrderCreateRequest;
import api.models.order.OrderUrgency;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class OrderTestData {
    private OrderTestData() {
    }

    private static final DateTimeFormatter OPEN_MRS_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String SIMPLE_DOSING_INSTRUCTIONS =
            "org.openmrs.SimpleDosingInstructions";

    public static OrderCreateRequest validOrder(
            EncounterResponse encounter
    ) {

        OffsetDateTime encounterDateTime =
                OffsetDateTime.parse(
                        encounter.encounterDatetime(),
                        OPEN_MRS_DATE_TIME
                );

        return validOrder(
                encounter,
                encounterDateTime
        );
    }

    public static OrderCreateRequest validOrder(
            EncounterResponse encounter,
            OffsetDateTime dateActivated
    ) {
        return OrderCreateRequest.builder()
                .type("testorder")
                .action(OrderAction.NEW.name())
                .urgency(OrderUrgency.ROUTINE.name())
                .dateActivated(dateActivated)
                .careSetting(CareSetting.OUTPATIENT.name())
                .encounter(encounter.uuid())
                .patient(encounter.patient().uuid())
                .concept(ReferenceTestData.pulseConceptId())
                .orderer(ReferenceTestData.encounterProviderUuid())
                .build();
    }

    public static DrugOrderCreateRequest validMinimalDrugOrder(
            EncounterResponse encounter
    ) {
        OffsetDateTime encounterDateTime =
                OffsetDateTime.parse(
                        encounter.encounterDatetime(),
                        OPEN_MRS_DATE_TIME
                );

        return DrugOrderCreateRequest.builder()
                .orderType(ReferenceTestData.drugOrderTypeUuid())
                .type("drugorder")
                .action(OrderAction.NEW.name())
                .urgency(OrderUrgency.ROUTINE.name())
                .dateActivated(encounterDateTime)
                .careSetting(CareSetting.OUTPATIENT.name())
                .encounter(encounter.uuid())
                .patient(encounter.patient().uuid())
                .concept(ReferenceTestData.amlodipineConceptUuid())
                .orderer(ReferenceTestData.encounterProviderUuid())
                .drug(ReferenceTestData.amlodipine5MgDrugUuid())
                .dosingType(SIMPLE_DOSING_INSTRUCTIONS)
                .dose(1.0)
                .doseUnits(ReferenceTestData.tabletConceptUuid())
                .route(ReferenceTestData.oralRouteConceptUuid())
                .frequency(ReferenceTestData.onceDailyFrequencyUuid())
                .quantity(1.0)
                .quantityUnits(ReferenceTestData.tabletConceptUuid())
                .numRefills(0)
                .build();
    }

    public static DrugOrderCreateRequest validFullDrugOrder(
            EncounterResponse encounter
    ) {
        return validMinimalDrugOrder(encounter)
                .toBuilder()
                .quantity(30.0)
                .numRefills(2)
                .duration(30)
                .durationUnits(ReferenceTestData.daysDurationUnitConceptUuid())
                .asNeeded(false)
                .dispenseAsWritten(false)
                .orderReasonNonCoded("Hypertension")
                .instructions("Take at the same time every day")
                .commentToFulfiller("Created by API test")
                .build();
    }
}
