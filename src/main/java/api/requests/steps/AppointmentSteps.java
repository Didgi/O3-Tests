package api.requests.steps;

import api.models.appointment.AppointmentCreateRequest;
import api.models.appointment.AppointmentPatientSearchRequest;
import api.models.appointment.AppointmentResponse;
import api.models.appointment.AppointmentSearchParams;
import api.models.appointment.AppointmentStatusChangeRequest;
import api.requests.endpoints.AppointmentEndpoints;
import api.requests.skeleton.interfaces.CrudEndpoint;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulPostSearchRequester;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class AppointmentSteps {
    private final SuccessfulSearchRequester<
            AppointmentSearchParams,
            AppointmentResponse
            > successfulSearchRequester;
    private final SuccessfulSearchRequester<
            AppointmentSearchParams,
            List<AppointmentResponse>
            > searchByDateRequester;
    private final SuccessfulPostSearchRequester<
            AppointmentPatientSearchRequest,
            List<AppointmentResponse>
            > searchByPatientRequester;
    private final CrudEndpoint<
            AppointmentCreateRequest,
            AppointmentCreateRequest
            > rawCrud;
    private final CrudEndpoint<
            AppointmentStatusChangeRequest,
            AppointmentStatusChangeRequest
            > statusChangeCrud;

    public AppointmentSteps(RequesterFactory requester) {
        this.successfulSearchRequester =
                requester.successfulSearch(AppointmentEndpoints.SEARCH);
        this.searchByDateRequester =
                requester.successfulSearch(AppointmentEndpoints.SEARCH_BY_DATE);
        this.searchByPatientRequester =
                requester.successfulPostSearch(AppointmentEndpoints.SEARCH_BY_PATIENT);
        this.rawCrud = requester.rawCrud(AppointmentEndpoints.CRUD);
        this.statusChangeCrud = requester.rawCrud(AppointmentEndpoints.STATUS_CHANGE_OPERATIONS);
    }

    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        return rawCrud.create(request)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(AppointmentEndpoints.CREATE.responseTypeRef());
    }

    public AppointmentResponse getAppointment(String uuid) {
        return successfulSearchRequester.search(new AppointmentSearchParams(uuid, null));
    }

    public List<AppointmentResponse> getAppointmentsForDate(String forDate) {
        return searchByDateRequester.search(new AppointmentSearchParams(null, forDate));
    }

    public List<AppointmentResponse> searchAppointmentsByPatient(
            String patientUuid,
            String startDate
    ) {
        return searchByPatientRequester.search(
                new AppointmentPatientSearchRequest(patientUuid, startDate)
        );
    }

    public AppointmentResponse changeAppointmentStatus(
            String uuid,
            AppointmentStatusChangeRequest request
    ) {
        return statusChangeCrud.update(uuid, request)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(AppointmentEndpoints.STATUS_CHANGE.responseTypeRef());
    }
}
