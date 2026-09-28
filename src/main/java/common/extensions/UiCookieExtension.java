package common.extensions;

import api.config.Config;
import api.models.auth.request.SessionLocation;
import api.models.auth.response.SessionResponse;
import api.models.user.UserCreateRequest;
import api.requests.skeleton.interfaces.AuthEndpoint;
import api.requests.skeleton.requesters.AuthRequester;
import api.specs.RequestSpecs;
import common.annotations.UiCookieAnnotation;
import io.restassured.response.Response;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import ui.pages.BasePage;

import java.util.Objects;

import static api.requests.endpoints.AuthEndpoints.SESSION;
import static org.apache.http.HttpStatus.SC_OK;

public class UiCookieExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(
            ExtensionContext context
    ) throws Exception {

        if (!hasUiCookieAnnotation(context)) {
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

        if (rawResponse.statusCode() != SC_OK || !rawResponse.body().as(SessionResponse.class).authenticated()) {
            throw new ExtensionConfigurationException(
                    "Ошибка получения валидной сессии"
            );
        }

        final String session = rawResponse.cookie(Config.getProperty("cookie_session_name"));

        BasePage.putSessionIntoCookie(session);

        final String testLocationUuid = Config.getProperty("test_location_uuid");

        final SessionLocation sessionLocation = new SessionLocation(testLocationUuid);
        final Response setLocationResponse = rawRequester.postLocation(sessionLocation, session);

        final SessionResponse sessionResponse = setLocationResponse.body().as(SessionResponse.class);

        if (setLocationResponse.statusCode() != SC_OK && !Objects.equals(sessionResponse.sessionLocation().uuid(),
                testLocationUuid)) {
            throw new ExtensionConfigurationException(
                    "Ошибка установки дефолтного значения локации"
            );
        }

    }

    private boolean hasUiCookieAnnotation(ExtensionContext context) {

        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                UiCookieAnnotation.class
        ) || AnnotationSupport.isAnnotated(
                context.getRequiredTestClass(),
                UiCookieAnnotation.class
        );
    }
}
