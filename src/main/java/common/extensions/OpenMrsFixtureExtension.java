package common.extensions;

import api.models.encounter.EncounterResponse;
import api.models.patients.PatientCreateRequest;
import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateResponse;
import api.requests.steps.ApiClient;
import api.testdata.EncounterTestData;
import api.testdata.PatientTestData;
import api.testdata.VisitTestData;
import common.annotations.WithEncounter;
import common.annotations.WithPatient;
import common.annotations.WithVisit;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

public final class OpenMrsFixtureExtension implements
        BeforeEachCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(OpenMrsFixtureExtension.class);
    private final ApiClient admin = ApiClient.admin();

    @Override
    public void beforeEach(
            ExtensionContext context
    ) throws Exception {

        if (!hasWithPatient(context)) {
            return;
        }

        ExtensionContext.Store store = context.getStore(NAMESPACE);

        PatientCreateRequest patientRequest =
                PatientTestData.validPatient();

        PatientResponse patient =
                admin.patients().createPatient(patientRequest);

        store.put(PatientCreateRequest.class, patientRequest);
        store.put(PatientResponse.class, patient);

        if (hasWithVisit(context)) {
            VisitCreateResponse visit = admin.visits().createVisit(
                    VisitTestData.activeVisitCreateRequest(patient.uuid())
            );

            store.put(VisitCreateResponse.class, visit);

            if (hasWithEncounter(context)) {
                EncounterResponse encounter = admin.encounters().createEncounter(
                        EncounterTestData.validEncounter(patient.uuid(), visit.uuid())
                );

                store.put(EncounterResponse.class, encounter);
            }
        }


    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        return (parameterContext.getParameter().getType() == PatientResponse.class
                && hasWithPatient(extensionContext))
                || (parameterContext.getParameter().getType() == PatientCreateRequest.class
                && hasWithPatient(extensionContext))
                || ((parameterContext.getParameter().getType() == VisitCreateResponse.class)
                && hasWithVisit(extensionContext))
                || (parameterContext.getParameter().getType() == EncounterResponse.class
                && hasWithEncounter(extensionContext));
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        Class<?> type = parameterContext.getParameter().getType();

        Object fixture = extensionContext.getStore(NAMESPACE)
                .get(type, type);

        if (fixture == null) {
            throw new ParameterResolutionException(
                    "Fixture " + type.getSimpleName()
                            + " was not created for test: "
                            + extensionContext.getDisplayName()
            );
        }

        return fixture;
    }

    private boolean hasWithPatient(ExtensionContext context) {
        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                WithPatient.class
        );
    }

    private boolean hasWithVisit(ExtensionContext context) {
        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                WithVisit.class
        );
    }

    private boolean hasWithEncounter(ExtensionContext context) {
        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                WithEncounter.class
        );
    }
}
