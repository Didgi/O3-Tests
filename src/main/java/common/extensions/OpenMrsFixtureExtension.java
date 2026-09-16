package common.extensions;

import api.models.patients.PatientResponse;
import api.requests.steps.ApiClient;
import api.testdata.PatientTestData;
import common.annotations.WithPatient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

public final class OpenMrsFixtureExtension implements
        BeforeEachCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(OpenMrsFixtureExtension.class);
    private final ApiClient admin = ApiClient.admin();
    private static final String PATIENT_KEY = "patient";

    @Override
    public void beforeEach(
            ExtensionContext context
    ) throws Exception {

        if(!hasWithPatient(context)) {
            return;
        }

        PatientResponse patient =
                admin.patients().createPatient(PatientTestData.validPatient());

        context.getStore(NAMESPACE).put(PATIENT_KEY, patient);
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        return parameterContext.getParameter().getType() == PatientResponse.class
                && hasWithPatient(extensionContext);
    }

    @Override
    public PatientResponse resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        PatientResponse patient = extensionContext.getStore(NAMESPACE).get(
                PATIENT_KEY,
                PatientResponse.class
        );

        if(patient == null) {
            throw new ParameterResolutionException(
                    "Patient fixture was not created for test: "
                    + extensionContext.getDisplayName()
            );
        }

        return patient;
    }

    private boolean hasWithPatient(ExtensionContext context) {
        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                WithPatient.class
        );
    }
}
