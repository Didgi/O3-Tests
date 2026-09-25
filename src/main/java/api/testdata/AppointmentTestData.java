package api.testdata;

import api.models.appointment.AppointmentCreateRequest;
import api.utils.RandomModelGenerator;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class AppointmentTestData {

    public static AppointmentCreateRequest validAppointmentCreateRequest(String patientUuid) {
        AppointmentCreateRequest generated = RandomModelGenerator.generate(AppointmentCreateRequest.class);
        return generated.toBuilder().patientUuid(patientUuid).build();
    }
}
