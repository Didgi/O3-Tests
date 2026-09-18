package common.extensions;

import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateResponse;
import api.requests.steps.ApiClient;
import api.testdata.PatientTestData;
import api.testdata.VisitTestData;
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

        PatientResponse patient =
                admin.patients().createPatient(PatientTestData.validPatient());

        store.put(PatientResponse.class, patient);

        if (hasWithVisit(context)) {
            VisitCreateResponse visit = admin.visits().createVisit(
                    VisitTestData.validVisitCreateRequest(patient.uuid())
            );

            store.put(VisitCreateResponse.class, visit);
        }


    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        return (parameterContext.getParameter().getType() == PatientResponse.class
                && hasWithPatient(extensionContext))
                || ((parameterContext.getParameter().getType() == VisitCreateResponse.class)
                && hasWithVisit(extensionContext));
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
}
