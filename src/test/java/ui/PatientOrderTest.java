package ui;

import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterResponse;
import api.models.order.DrugOrderCreateRequest;
import api.models.order.DrugOrderResponse;
import api.models.order.OrderAction;
import api.models.order.OrderSearchResponse;
import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateRequest;
import api.models.visit.VisitCreateResponse;
import api.requests.skeleton.options.ReadOptions;
import api.testdata.EncounterTestData;
import api.testdata.OrderTestData;
import api.testdata.VisitTestData;
import api.utils.RandomData;
import common.annotations.WithEncounter;
import common.annotations.WithPatient;
import org.junit.jupiter.api.Test;
import ui.elements.order.OrderStatus;
import ui.models.DrugOrderData;
import ui.models.ExpectedOrderRow;
import ui.pages.PatientOrderPage;
import ui.testdata.DrugOrderTestData;

import java.time.OffsetDateTime;

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
                .waitUntilLoaded()
                .openOrderBasket()
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

    @WithPatient
    @Test
    void shouldDisplayExistingDrugOrder(PatientResponse patient) {

        OffsetDateTime orderDateTime = RandomData.randomPastDateTime();

        String apiDate = orderDateTime.format(ON_DATE_FORMAT);

        VisitCreateRequest visitRequest =
                VisitTestData.validVisitCreateRequest(patient.uuid()).toBuilder()
                        .startDatetime(apiDate)
                        .build();
        VisitCreateResponse visit = admin.visits().createVisit(visitRequest);

        EncounterCreateRequest encounterRequest =
                EncounterTestData.withEncounterDatetime(
                        EncounterTestData.validEncounter(patient.uuid(), visit.uuid()),
                        apiDate
                );
        EncounterResponse encounter =
                admin.encounters().createEncounter(encounterRequest);


        DrugOrderCreateRequest request =
                OrderTestData.validMinimalDrugOrder(encounter);
        DrugOrderResponse response =
                admin.orders().createDrugOrder(request);

        ExpectedOrderRow expected = new ExpectedOrderRow(
                response.orderNumber(),
                response.orderType().display(),
                response.display(),
                response.urgency().name(),
                response.orderer().display()
        );

        new PatientOrderPage(encounter.patient().uuid())
                .open()
                .waitUntilLoaded()
                .setFilterStartDate(orderDateTime)
                .shouldContainExistingOrder(expected, response.dateActivated());
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
                .waitUntilLoaded()
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
                .waitUntilLoaded()
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
