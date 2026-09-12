package api_tests.p0;

import api.config.Config;
import api.models.auth.request.Credentials;
import api.models.auth.response.SessionResponse;
import api.requests.skelethon.interfaces.AuthEndpoint;
import api.requests.skelethon.requesters.AuthRequester;
import api.requests.skelethon.requesters.SuccessfulAuthRequester;
import api.specs.RequestSpecs;
import api.testdata.PatientTestData;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static api.requests.skelethon.endpoints.AuthEndpoints.SESSION;

@EnabledIfSystemProperty(named = "openmrs.integration.enabled", matches = "true")
public class AuthTests extends BaseApiTest {

    @Test
    public void getAuthSessionWithValidDataAdmin() {
        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);
        SuccessfulAuthRequester requester = new SuccessfulAuthRequester(rawRequester, SESSION);

        SessionResponse session = requester.getSession(new Credentials(
                Config.getProperty("admin_username"),
                Config.getProperty("admin_password")
        ));

        softly.assertThat(session.authenticated()).isTrue();
        softly.assertThat(session.user()).isNotNull();
    }

    private static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of(new Faker().name().firstName(), Config.getProperty("admin_password")),
                Arguments.of(Config.getProperty("admin_username"), new Faker().name().lastName()),
                Arguments.of(new Faker().name().firstName(), new Faker().name().lastName())
        );
    }

    @MethodSource("invalidCredentials")
    @ParameterizedTest
    public void getAuthSessionWithInvalidDataAdmin(String username, String password) {
        AuthEndpoint requester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        Response response = requester.getSession(new Credentials(username, password));

        response.then().statusCode(200);
        softly.assertThat(response.jsonPath().getBoolean("authenticated")).isFalse();
    }
}
