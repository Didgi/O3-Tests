package ui;

import api.models.encounter.EncounterResponse;
import api.models.order.OrderSearchResponse;
import common.annotations.WithEncounter;
import org.junit.jupiter.api.Test;
import ui.elements.order.OrderStatus;
import ui.models.DrugOrderData;
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
}
