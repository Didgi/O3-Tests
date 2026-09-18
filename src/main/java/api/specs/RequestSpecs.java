package api.specs;

import api.config.Config;
import api.requests.skeleton.interfaces.AuthEndpoint;
import api.requests.skeleton.requesters.AuthRequester;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Paths;
import java.util.List;

import static api.requests.endpoints.AuthEndpoints.SESSION;
import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public final class RequestSpecs {
    static final String SESSION_COOKIE_NAME = "JSESSIONID";

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter(), new AllureRestAssured(),
                        new SwaggerCoverageRestAssured(
                                new FileSystemOutputWriter(Paths.get("target/" + OUTPUT_DIRECTORY))
                        )))
                .setBaseUri(Config.getProperty("api_baseurl") + Config.getProperty("api_version"));

    }

    public static RequestSpecification baseRequest() {
        return defaultRequestSpec().build();
    }

    public static RequestSpecification withAdminBasicAuth() {
        return withBasicAuth(
                Config.getProperty("admin_username"),
                Config.getProperty("admin_password")
        );
    }

    public static RequestSpecification withAuth(String username, String password) {
        return withBasicAuth(username, password);
    }

    private static RequestSpecification withBasicAuth(
            String username,
            String password
    ) {
        return defaultRequestSpec()
                .build()
                .auth()
                .preemptive()
                .basic(username, password);
    }

    public static RequestSpecification withCookie(String cookieValue) {
        return defaultRequestSpec().addCookie(SESSION_COOKIE_NAME, cookieValue).build();
    }

    public static RequestSpecification withAdminCookie() {
        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.withAdminBasicAuth(), SESSION);
        final String cookie = rawRequester.getSession().cookie(SESSION_COOKIE_NAME);

        return withCookie(cookie);
    }

}
