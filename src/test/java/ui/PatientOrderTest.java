package ui;

import api.models.encounter.EncounterResponse;
import api.models.order.DrugOrderCreateRequest;
import api.models.order.DrugOrderResponse;
import api.models.order.OrderAction;
import api.models.order.OrderSearchResponse;
import api.testdata.OrderTestData;
import common.annotations.WithEncounter;
import org.junit.jupiter.api.Test;
import ui.elements.order.OrderStatus;
import ui.models.DrugOrderData;
import ui.models.ExpectedOrderRow;
import ui.pages.PatientOrderPage;
import ui.testdata.DrugOrderTestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PatientOrderTest extends UIBaseTest {

    @WithEncounter
    @Test
    void shouldAddDrugOrderWithValidData(
            EncounterResponse encounter
    ) {

        DrugOrderData order = DrugOrderTestData.validAcetaminophenOrder();

        new PatientOrderPage(encounter.patient().uuid())
                .open()
                .checkPatientOrderOpen()
                .openOrderBasket()
                .checkBasketFormIsOpened()
                .clickAddDrugOrder()
                .addDrugToOrderForm(order.drugName())
                .shouldHaveCorrectDrug(order.drugName())
                .fillAndSubmit(order)
                .shouldHaveDrugOrder(order, OrderStatus.NEW)
                .submitOrder();

        OrderSearchResponse orderSearch =
                admin.orders().searchOrderByPatient(encounter.patient().uuid());

        softly.assertThat(orderSearch.results())
                .isNotEmpty();
        softly.assertThat(orderSearch.results())
                .extracting(OrderSearchResponse.OrderItem::display)
                .anySatisfy(display ->
                        assertThat(display).contains(order.drugName())
                );

    }

    @WithEncounter
    @Test
    void shouldDisplayExistingDrugOrder(EncounterResponse encounter) {

        DrugOrderCreateRequest request =
                OrderTestData.validMinimalDrugOrder(encounter);
        DrugOrderResponse response =
                admin.orders().createDrugOrder(request);

        ExpectedOrderRow expected = new ExpectedOrderRow(
                response.orderNumber(),
                response.urgency(),
                response.display()
        );

        new PatientOrderPage(encounter.patient().uuid())
                .open()
                .shouldContainExistingOrder(expected);

    }

    @WithEncounter
    @Test
    void shouldModifyRefillsExistingOrder(EncounterResponse encounter) {
        DrugOrderCreateRequest request =
                OrderTestData.validFullDrugOrder(encounter);
        DrugOrderResponse response =
                admin.orders().createDrugOrder(request);
        int expectedRefills = request.numRefills() + 1;

        new PatientOrderPage(encounter.patient().uuid())
                .open()
                .openOrderForEditing(response.orderNumber())
                .setPrescriptionRefills(String.valueOf(expectedRefills))
                .saveOrder()
                .shouldShowOrderUpdated(response.drug().display());

        OrderSearchResponse modifiedOrderSearch =
                admin.orders().searchOrderByPatient(
                        encounter.patient().uuid()
                );

        List<DrugOrderResponse> revisions = modifiedOrderSearch.results()
                .stream()
                .map(orderItem ->
                        admin.orders().getDrugOrder(orderItem.uuid())
                )
                .filter(order ->
                        OrderAction.REVISE.name().equals(order.action())
                )
                .filter(order -> order.previousOrder() != null)
                .filter(order -> response.uuid().equals(
                        order.previousOrder().uuid()
                ))
                .toList();

        assertThat(revisions)
                .as("revision of order %s", response.uuid())
                .singleElement()
                .satisfies(modifiedOrder -> {
                    softly.assertThat(modifiedOrder.numRefills())
                            .isEqualTo(expectedRefills);
                    softly.assertThat(modifiedOrder.previousOrder().uuid())
                            .isEqualTo(response.uuid());
                });
    }
}
