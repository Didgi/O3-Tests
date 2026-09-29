package api;

import api.models.encounter.EncounterResponse;
import api.models.order.OrderCreateRequest;
import api.models.order.OrderResponse;
import api.testdata.OrderTestData;
import common.annotations.WithEncounter;
import org.junit.jupiter.api.Test;

public class OrderTest extends BaseApiTest {
    @WithEncounter
    @Test
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
}
