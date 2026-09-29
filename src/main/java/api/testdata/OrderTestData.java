package api.testdata;

import api.models.encounter.EncounterResponse;
import api.models.order.CareSetting;
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
                .action(OrderAction.NEW.value())
                .urgency(OrderUrgency.ROUTINE.name())
                .dateActivated(dateActivated)
                .careSetting(CareSetting.OUTPATIENT.name())
                .encounter(encounter.uuid())
                .patient(encounter.patient().uuid())
                .concept(ReferenceTestData.pulseConceptId())
                .orderer(ReferenceTestData.encounterProviderUuid())
                .build();
    }
}
