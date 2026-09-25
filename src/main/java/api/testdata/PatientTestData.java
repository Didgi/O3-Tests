package api.testdata;

import api.models.patients.*;
import api.specs.RequestSpecs;
import api.utils.RandomData;
import api.utils.RandomModelGenerator;

import java.util.List;

import static io.restassured.RestAssured.given;

public class PatientTestData {

    private static final String NON_EXISTING_QUERY_PREFIX = "NonExisting_";

    private static final int TOO_OLD_YEARS = 150;

    private PatientTestData() {
    }

    public static String generateIdentifier() {
        return given()
                .spec(RequestSpecs.withAdminBasicAuth())
                .pathParam(
                        "sourceUuid",
                        ReferenceTestData.patientIdentifierSourceUuid()
                )
                .body("{}")
                .post(
                        "/idgen/identifiersource/"
                                + "{sourceUuid}/identifier"
                )
                .then()
                .statusCode(201)
                .extract()
                .path("identifier");
    }

    public static PatientCreateRequest validPatient() {
        PatientIdentifierRequest identifier =
                new PatientIdentifierRequest(
                        generateIdentifier(),
                        ReferenceTestData.patientIdentifierTypeUuid(),
                        ReferenceTestData.locationUuid(),
                        true
                );

        PatientPersonRequest person =
                RandomModelGenerator.generate(PatientPersonRequest.class);

        return new PatientCreateRequest(
                List.of(identifier),
                person
        );
    }

    public static PatientCreateRequest patientWithExistingIdentifier(
            String identifierValue
    ) {
        PatientCreateRequest validRequest = validPatient();

        PatientIdentifierRequest validIdentifier =
                validRequest.identifiers().getFirst();

        PatientIdentifierRequest duplicateIdentifier =
                new PatientIdentifierRequest(
                        identifierValue,
                        validIdentifier.identifierType(),
                        validIdentifier.location(),
                        validIdentifier.preferred()
                );

        return new PatientCreateRequest(
                List.of(duplicateIdentifier),
                validRequest.person()
        );
    }

    public static PatientCreateRequest patientWithoutName() {
        PatientCreateRequest validRequest = validPatient();

        PatientPersonRequest person =
                copyPerson(
                        validRequest.person(),
                        validRequest.person().birthdate(),
                        validRequest.person().gender(),
                        List.of()
                );

        return new PatientCreateRequest(
                validRequest.identifiers(),
                person
        );
    }

    public static PatientCreateRequest patientWithoutGender() {
        PatientCreateRequest validRequest = validPatient();

        PatientPersonRequest person =
                copyPerson(
                        validRequest.person(),
                        validRequest.person().birthdate(),
                        null,
                        validRequest.person().names()
                );

        return new PatientCreateRequest(
                validRequest.identifiers(),
                person
        );
    }

    public static PatientCreateRequest patientWithoutIdentifier() {
        PatientCreateRequest validRequest = validPatient();

        return new PatientCreateRequest(
                List.of(),
                validRequest.person()
        );
    }

    public static PatientCreateRequest patientWithFutureBirthdate() {
        PatientCreateRequest validRequest = validPatient();

        PatientPersonRequest person =
                copyPerson(
                        validRequest.person(),
                        RandomData.futureDate(),
                        validRequest.person().gender(),
                        validRequest.person().names()
                );

        return new PatientCreateRequest(
                validRequest.identifiers(),
                person
        );
    }

    public static PatientCreateRequest patientWithTooOldBirthdate() {
        PatientCreateRequest validRequest = validPatient();

        PatientPersonRequest person =
                copyPerson(
                        validRequest.person(),
                        RandomData.pastDateYears(TOO_OLD_YEARS),
                        validRequest.person().gender(),
                        validRequest.person().names()
                );

        return new PatientCreateRequest(
                validRequest.identifiers(),
                person
        );
    }

    public static PatientCreateRequest patientWithInvalidIdentifier() {
        PatientCreateRequest validRequest = validPatient();

        PatientIdentifierRequest validIdentifier =
                validRequest.identifiers().getFirst();

        PatientIdentifierRequest invalidIdentifier =
                new PatientIdentifierRequest(
                        RandomData.changeLastCharacter(
                                validIdentifier.identifier()
                        ),
                        validIdentifier.identifierType(),
                        validIdentifier.location(),
                        validIdentifier.preferred()
                );

        return new PatientCreateRequest(
                List.of(invalidIdentifier),
                validRequest.person()
        );
    }

    public static PatientCreateRequest patientWithNonExistingIdentifierType() {
        PatientCreateRequest validRequest = validPatient();

        PatientIdentifierRequest validIdentifier =
                validRequest.identifiers().getFirst();

        PatientIdentifierRequest invalidIdentifier =
                new PatientIdentifierRequest(
                        validIdentifier.identifier(),
                        RandomData.randomUuid(),
                        validIdentifier.location(),
                        validIdentifier.preferred()
                );

        return new PatientCreateRequest(
                List.of(invalidIdentifier),
                validRequest.person()
        );
    }

    public static PatientCreateRequest patientWithNonExistingLocation() {
        PatientCreateRequest validRequest = validPatient();

        PatientIdentifierRequest validIdentifier =
                validRequest.identifiers().getFirst();

        PatientIdentifierRequest invalidIdentifier =
                new PatientIdentifierRequest(
                        validIdentifier.identifier(),
                        validIdentifier.identifierType(),
                        RandomData.randomUuid(),
                        validIdentifier.preferred()
                );

        return new PatientCreateRequest(
                List.of(invalidIdentifier),
                validRequest.person()
        );
    }

    public static PatientCreateRequest patientWithoutBirthdate() {
        PatientCreateRequest validRequest = validPatient();

        PatientPersonRequest person =
                copyPerson(
                        validRequest.person(),
                        null,
                        validRequest.person().gender(),
                        validRequest.person().names()
                );

        return new PatientCreateRequest(
                validRequest.identifiers(),
                person
        );
    }

    public static String nonExistingUuid() {
        return RandomData.randomUuid();
    }

    public static String updatedGivenName() {
        return RandomModelGenerator.generate(PatientNameRequest.class)
                .givenName();
    }

    public static String nonExistingQuery() {
        return RandomData.uniqueValue(
                NON_EXISTING_QUERY_PREFIX
        );
    }

    public static PatientUpdateRequest updateName(
            PatientCreateRequest original,
            PatientResponse created,
            String newGivenName
    ) {
        PatientIdentifierRequest originalIdentifier =
                original.identifiers().getFirst();

        PatientNameRequest originalName =
                original.person().names().getFirst();

        PatientIdentifierUpdateRequest identifier =
                new PatientIdentifierUpdateRequest(
                        created.identifiers().getFirst().uuid(),
                        originalIdentifier.identifier(),
                        originalIdentifier.identifierType(),
                        originalIdentifier.location(),
                        originalIdentifier.preferred()
                );

        PatientNameUpdateRequest name =
                new PatientNameUpdateRequest(
                        created.person().preferredName().uuid(),
                        originalName.preferred(),
                        newGivenName,
                        originalName.middleName(),
                        originalName.familyName()
                );

        PatientPersonUpdateRequest person =
                new PatientPersonUpdateRequest(
                        created.person().uuid(),
                        original.person().addresses(),
                        original.person().attributes(),
                        original.person().birthdate(),
                        original.person().birthdateEstimated(),
                        original.person().dead(),
                        original.person().gender(),
                        List.of(name)
                );

        return new PatientUpdateRequest(
                created.uuid(),
                List.of(identifier),
                person
        );
    }

    private static PatientPersonRequest copyPerson(
            PatientPersonRequest original,
            String birthdate,
            String gender,
            List<PatientNameRequest> names
    ) {
        return new PatientPersonRequest(
                original.addresses(),
                original.attributes(),
                birthdate,
                original.birthdateEstimated(),
                original.dead(),
                gender,
                names
        );
    }
}
