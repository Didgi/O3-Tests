package api.visit;

import api.BaseApiTest;
import api.models.visit.VisitCreateRequest;
import api.models.visit.VisitCreateResponse;
import api.models.visit.VisitUpdateRequest;
import api.requests.steps.ApiClient;
import api.testdata.PatientTestData;
import api.testdata.VisitTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class VisitTest extends BaseApiTest {
    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @Test
    @DisplayName("Создание нового visit")
    void adminCanCreateVisit() {
        String patientUuid = admin.patients().createPatient(PatientTestData.validPatient()).uuid();
        VisitCreateRequest request = VisitTestData.validVisitCreateRequest(patientUuid);

        VisitCreateResponse created = admin.visits().createVisit(request);

        softly.assertThat(created)
                .as("Request model fields are equal response model fields")
                .extracting(
                        response -> response.patient().uuid(),
                        response -> response.visitType().uuid(),
                        response -> response.location().uuid())
                .containsExactly(
                        request.patient(),
                        request.visitType(),
                        request.location());

        softly.assertThat(created.startDatetime())
                .as("OpenMRS assigns startDatetime when it is omitted")
                .isNotBlank();
        softly.assertThat(created.voided())
                .as("Newly created visit is not voided")
                .isFalse();

        VisitCreateResponse fetched = admin.visits().getVisit(created.uuid());

        softly.assertThat(fetched)
                .as("Request model fields are equal response model fields")
                .extracting(
                        response -> response.patient().uuid(),
                        response -> response.visitType().uuid(),
                        response -> response.location().uuid())
                .containsExactly(
                        request.patient(),
                        request.visitType(),
                        request.location());

        admin.visits().deleteVisit(created.uuid());
    }

    @Test
    @Disabled("BUG: Запись окончания приема записывается в +0000 зоне")
    @DisplayName("Завершение Visit")
    void adminCanStopVisit() {
        String patientUuid = admin.patients().createPatient(PatientTestData.validPatient()).uuid();
        VisitCreateRequest request = VisitTestData.validVisitCreateRequest(patientUuid);

        VisitCreateResponse created = admin.visits().createVisit(request);

        softly.assertThat(created.stopDatetime())
                .as("Newly created visit is not stopped")
                .isNull();

        String stopDatetime = ZonedDateTime.now(ZoneId.of(TIME_ZONE)).format(ON_DATE_FORMAT);
        VisitCreateResponse stopped = admin.visits().endVisit(
                created.uuid(),
                new VisitUpdateRequest(stopDatetime)
        );

        VisitCreateResponse fetched = admin.visits().getVisit(created.uuid());

        softly.assertThat(fetched.stopDatetime())
                .as("GET returns a stopped visit")
                .isEqualTo(stopDatetime);
    }

}
