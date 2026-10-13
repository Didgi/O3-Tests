package api;

import api.models.encounter.EncounterResponse;
import api.models.order.DrugOrderCreateRequest;
import api.models.order.DrugOrderResponse;
import api.models.order.OrderCreateRequest;
import api.models.order.OrderResponse;
import api.testdata.OrderTestData;
import common.annotations.WithEncounter;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OrderTest extends BaseApiTest {

    @Test
    @Issue("TRACK-008")
    @Severity(SeverityLevel.CRITICAL)
    @WithEncounter
    @DisplayName("Создание заказа")
    void createOrderTest(EncounterResponse encounter) {
        OrderCreateRequest request = OrderTestData.validOrder(encounter);
        OrderResponse response = admin.orders().createOrder(request);

        softly.assertThat(response.uuid())
                .isNotNull();
        softly.assertThat(response.patient().uuid())
                .isEqualTo(encounter.patient().uuid());
        softly.assertThat(response.encounter().uuid())
                .isEqualTo(encounter.uuid());
    }

    @Test
    @Issue("TRACK-008")
    @Severity(SeverityLevel.CRITICAL)
    @WithEncounter
    @DisplayName("Создание заказа на лекарство")
    void createDrugOrderTest(
            EncounterResponse encounter
    ) {
        DrugOrderCreateRequest request = OrderTestData.validMinimalDrugOrder(encounter);
        DrugOrderResponse response = admin.orders().createDrugOrder(request);

        softly.assertThat(response.uuid())
                .isNotNull();
        softly.assertThat(response.orderNumber())
                .isNotBlank();
        softly.assertThat(response.type())
                .isEqualTo(request.type());
        softly.assertThat(response.action())
                .isEqualTo(request.action());
        softly.assertThat(response.urgency().name())
                .isEqualTo(request.urgency());
        softly.assertThat(response.dateActivated())
                .isEqualTo(request.dateActivated());
        softly.assertThat(response.patient().uuid())
                .isEqualTo(request.patient());
        softly.assertThat(response.encounter().uuid())
                .isEqualTo(request.encounter());
        softly.assertThat(response.concept().uuid())
                .isEqualTo(request.concept());
        softly.assertThat(response.drug().uuid())
                .isEqualTo(request.drug());
        softly.assertThat(response.dose())
                .isEqualTo(request.dose());
        softly.assertThat(response.doseUnits().uuid())
                .isEqualTo(request.doseUnits());
        softly.assertThat(response.route().uuid())
                .isEqualTo(request.route());
        softly.assertThat(response.frequency().uuid())
                .isEqualTo(request.frequency());
        softly.assertThat(response.quantity())
                .isEqualTo(request.quantity());
        softly.assertThat(response.quantityUnits().uuid())
                .isEqualTo(request.quantityUnits());
        softly.assertThat(response.numRefills())
                .isEqualTo(request.numRefills());
    }
}
