package common.extensions;

import api.config.Config;
import api.requests.skeleton.interfaces.AuthEndpoint;
import api.requests.skeleton.requesters.AuthRequester;
import api.specs.RequestSpecs;
import common.annotations.AdminSession;
import io.restassured.response.Response;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import ui.pages.BasePage;

import static api.requests.endpoints.AuthEndpoints.SESSION;

public class AdminSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(
            ExtensionContext context
    ) throws Exception {

        if (!hasAdminSession(context)) {
            return;
        }

        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.withAdminBasicAuth(), SESSION);
        final Response rawResponse = rawRequester.getSession();
        final String adminCoookie = rawResponse.cookie(Config.getProperty("cookie_session_name"));

        BasePage.putSessionIntoCookie(adminCoookie);

    }

    private boolean hasAdminSession(ExtensionContext context) {
        return AnnotationSupport.isAnnotated(
                context.getRequiredTestMethod(),
                AdminSession.class
        );
    }
}
