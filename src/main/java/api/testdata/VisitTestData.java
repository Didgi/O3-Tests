package api.testdata;

import api.models.appointment.AppointmentCreateRequest;
import api.models.visit.VisitCreateRequest;
import api.utils.RandomModelGenerator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class VisitTestData {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    ReferenceTestData.dateTimeFormat()
            );

    private VisitTestData() {
    }

    public static VisitCreateRequest validVisitCreateRequest(
            String patientUuid
    ) {
        VisitCreateRequest generated = RandomModelGenerator.generate(VisitCreateRequest.class);
        return generated.toBuilder().patient(patientUuid).build();
    }

    public static VisitCreateRequest activeVisitCreateRequest(
            String patientUuid
    ) {
        String startDatetime = ZonedDateTime.now(
                        ZoneId.of(ReferenceTestData.timeZone())
                )
                .minusMinutes(5)
                .format(DATE_TIME_FORMAT);

        VisitCreateRequest generated = RandomModelGenerator.generate(VisitCreateRequest.class);
        return generated.toBuilder()
                .patient(patientUuid)
                .startDatetime(startDatetime)
                .build();
    }
}
