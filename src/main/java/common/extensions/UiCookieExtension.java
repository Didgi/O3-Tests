package common.extensions;

import api.config.Config;
import api.models.user.UserCreateRequest;
import api.requests.skeleton.interfaces.AuthEndpoint;
import api.requests.skeleton.requesters.AuthRequester;
import api.specs.RequestSpecs;
import common.annotations.WithUser;
import io.restassured.response.Response;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import ui.pages.BasePage;

import static api.requests.endpoints.AuthEndpoints.SESSION;
import static org.apache.http.HttpStatus.SC_OK;

public class UiCookieExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(
            ExtensionContext context
    ) throws Exception {

        if (!hasWithUser(context)) {
            return;
        }

        final ExtensionContext.Store store = context.getStore(UserExtensions.NAMESPACE);
        final UserCreateRequest userCreateRequest = store.get(UserCreateRequest.class, UserCreateRequest.class);

        if (userCreateRequest == null) {
            throw new ExtensionConfigurationException(
                    "UserCreateRequest отсутствует"
            );
        }

        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.withAuth(userCreateRequest.username(), userCreateRequest.password()), SESSION);

        final Response rawResponse = rawRequester.getSession();

        if (rawResponse.statusCode() != SC_OK) {
            throw new ExtensionConfigurationException(
                    "Ошибка авторизации: HTTP " + rawResponse.statusCode()
            );
        }

        final String session = rawResponse.cookie(Config.getProperty("cookie_session_name"));

        BasePage.putSessionIntoCookie(session);

    }

    private boolean hasWithUser(ExtensionContext context) {

        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                WithUser.class
        ) || AnnotationSupport.isAnnotated(
                context.getRequiredTestClass(),
                WithUser.class
        );
    }
}
