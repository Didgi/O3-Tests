package ui;

import api.models.encounter.EncounterResponse;
import api.models.order.DrugOrderCreateRequest;
import api.models.order.DrugOrderResponse;
import api.models.order.OrderAction;
import api.models.order.OrderSearchResponse;
import api.requests.skeleton.options.ReadOptions;
import api.testdata.OrderTestData;
import common.annotations.WithEncounter;
import org.junit.jupiter.api.Test;
import ui.elements.order.OrderStatus;
import ui.models.DrugOrderData;
import ui.models.ExpectedOrderRow;
import ui.pages.PatientOrderPage;
import ui.testdata.DrugOrderTestData;

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
                .signAndClose();

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
                        encounter.patient().uuid(),
                        ReadOptions.custom(
                                "uuid,action,previousOrder:(uuid),numRefills"
                        )
                );

        assertThat(modifiedOrderSearch.results())
                .as("revision of order %s", response.uuid())
                .filteredOn(order ->
                        order.previousOrder() != null
                                && response.uuid().equals(
                                order.previousOrder().uuid()
                        )
                )
                .singleElement()
                .extracting(
                        OrderSearchResponse.OrderItem::action,
                        OrderSearchResponse.OrderItem::numRefills
                )
                .containsExactly(
                        OrderAction.REVISE.name(),
                        expectedRefills
                );
    }

    @Test
    @WithEncounter
    void shouldCancelExistingOrder(EncounterResponse encounter) {
        DrugOrderCreateRequest request =
                OrderTestData.validFullDrugOrder(encounter);
        DrugOrderResponse response =
                admin.orders().createDrugOrder(request);

        new PatientOrderPage(encounter.patient().uuid())
                .open()
                .startOrderCancellation(response.orderNumber())
                .shouldHaveDrugOrder(
                        response.drug().display(),
                        OrderStatus.DISCONTINUE
                )
                .signAndClose();

        OrderSearchResponse cancelledOrderSearch =
                admin.orders().searchOrderByPatient(
                        encounter.patient().uuid(),
                        ReadOptions.custom(
                                "uuid,action,previousOrder:(uuid),dateStopped"
                        )
                );

        assertThat(cancelledOrderSearch.results())
                .as("discontinuation of order %s", response.uuid())
                .filteredOn(order ->
                        order.previousOrder() != null
                                && response.uuid().equals(
                                order.previousOrder().uuid()
                        )
                )
                .singleElement()
                .extracting(OrderSearchResponse.OrderItem::action)
                .isEqualTo(OrderAction.DISCONTINUE.name());

        assertThat(cancelledOrderSearch.results())
                .as("stopped original order %s", response.uuid())
                .filteredOn(order ->
                        response.uuid().equals(order.uuid())
                )
                .singleElement()
                .extracting(OrderSearchResponse.OrderItem::dateStopped)
                .isNotNull();
    }
}
