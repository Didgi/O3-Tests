package common.extensions;

import api.config.Config;
import api.models.user.UserCreateRequest;
import api.models.user.UserCreateResponse;
import api.requests.steps.ApiClient;
import api.utils.RandomModelGenerator;
import common.annotations.WithUser;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

public final class UserExtensions implements
        BeforeEachCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserExtensions.class);

    private final ApiClient admin = ApiClient.admin();

    @Override
    public void beforeEach(ExtensionContext context) {

        Optional<WithUser> annotation = findWithUser(context);

        if (annotation.isEmpty()) {
            return;
        }

        ExtensionContext.Store store = context.getStore(NAMESPACE);

        switch (annotation.get().role()) {

            case SUPER_ADMIN -> {
                UserCreateRequest request = UserCreateRequest
                        .builder()
                        .username(Config.getProperty("ADMIN_USERNAME"))
                        .password(Config.getProperty("ADMIN_PASSWORD"))
                        .build();

                store.put(UserCreateRequest.class, request);
            }

            default -> {
                UserCreateRequest request =
                        RandomModelGenerator.generate(
                                UserCreateRequest.class
                        );

                UserCreateResponse response =
                        admin.users().createUser(request);

                store.put(UserCreateRequest.class, request);
                store.put(UserCreateResponse.class, response);
            }
        }
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        Class<?> type = parameterContext
                .getParameter()
                .getType();

        boolean supportedType =
                type == UserCreateRequest.class
                        || type == UserCreateResponse.class;

        return supportedType
                && findWithUser(extensionContext).isPresent();
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {

        Class<?> type = parameterContext
                .getParameter()
                .getType();

        Object fixture = extensionContext
                .getStore(NAMESPACE)
                .get(type, type);

        if (fixture == null) {
            throw new ParameterResolutionException(
                    "Fixture " + type.getSimpleName()
                            + " was not created for test: "
                            + extensionContext.getDisplayName()
                            + ". Check @WithUser role and fixture creation."
            );
        }

        return fixture;
    }

    private Optional<WithUser> findWithUser(
            ExtensionContext context
    ) {

        Optional<WithUser> methodAnnotation =
                context.getTestMethod()
                        .flatMap(method ->
                                AnnotationSupport.findAnnotation(
                                        method,
                                        WithUser.class
                                )
                        );

        if (methodAnnotation.isPresent()) {
            return methodAnnotation;
        }

        return context.getTestClass()
                .flatMap(testClass ->
                        AnnotationSupport.findAnnotation(
                                testClass,
                                WithUser.class
                        )
                );
    }
}