package api.testdata;

import api.models.visit.VisitCreateRequest;

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
        return new VisitCreateRequest(
                patientUuid,
                ReferenceTestData.visitTypeUuid(),
                null,
                ReferenceTestData.locationUuid()
        );
    }

    public static VisitCreateRequest activeVisitCreateRequest(
            String patientUuid
    ) {
        String startDatetime = ZonedDateTime.now(
                        ZoneId.of(ReferenceTestData.timeZone())
                )
                .minusMinutes(5)
                .format(DATE_TIME_FORMAT);

        return new VisitCreateRequest(
                patientUuid,
                ReferenceTestData.visitTypeUuid(),
                startDatetime,
                ReferenceTestData.locationUuid()
        );
    }
}
