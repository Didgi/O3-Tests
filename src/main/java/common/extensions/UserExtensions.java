package common.extensions;

import api.models.user.UserCreateRequest;
import api.models.user.UserCreateResponse;
import api.requests.steps.ApiClient;
import api.utils.RandomModelGenerator;
import common.annotations.WithUser;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

public final class UserExtensions implements
        BeforeEachCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserExtensions.class);
    private final ApiClient admin = ApiClient.admin();

    @Override
    public void beforeEach(
            ExtensionContext context
    ) throws Exception {

        if (!hasWithUser(context)) {
            return;
        }

        ExtensionContext.Store store = context.getStore(NAMESPACE);
        UserCreateRequest userCreateRequest = RandomModelGenerator.generate(UserCreateRequest.class);
        final UserCreateResponse userCreateResponse = admin.users().createUser(userCreateRequest);
        store.put(UserCreateRequest.class, userCreateRequest);
        store.put(UserCreateResponse.class, userCreateResponse);
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        return (parameterContext.getParameter().getType() == UserCreateResponse.class
                || parameterContext.getParameter().getType() == UserCreateRequest.class
                && hasWithUser(extensionContext));
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

    private boolean hasWithUser(ExtensionContext context) {
        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                WithUser.class
        );
    }
}
