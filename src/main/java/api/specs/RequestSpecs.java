package api.specs;

import api.config.Config;
import api.requests.skelethon.EndpointRequests;
import api.requests.skelethon.requesters.CrudRequester;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import com.google.common.net.HttpHeaders;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.config.ApiPath.BASE_URI;
import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public class RequestSpecs {

    private static Map<String, String> sessionStorage = new HashMap<>();
    private static final String ADMIN_USERNAME = Config.getProperty("admin_username");
    private static final String ADMIN_PASSWORD = Config.getProperty("admin_password");
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final String ADMIN_AUTH_BASIC = "Basic YWRtaW46QWRtaW4xMjM=";
    private static final String ADMIN_AUTH_SESSION_ID = "1F228C2C84CA7986DEC5A6ED57AE6ADB";

    private RequestSpecs() {
    }

    public static RequestSpecBuilder basicRequestSpec() {
        return new RequestSpecBuilder().setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter(), new AllureRestAssured(),
                        new SwaggerCoverageRestAssured(
                                new FileSystemOutputWriter(Paths.get("target/" + OUTPUT_DIRECTORY))
                        )))
                .setBaseUri(BASE_URI);
    }

    public static RequestSpecification withoutAuthSpec() {
        return basicRequestSpec().build();
    }

    public static RequestSpecification withLoginPass(String username, String password) {
        return basicRequestSpec().build()
                .auth()
                .preemptive()
                .basic(username, password);
    }

    public static RequestSpecification withAllNeededAuthData(String sessionId, String username, String password) {
        return basicRequestSpec().build()
                .auth()
                .preemptive()
                .basic(username, password)
                .headers(HttpHeaders.COOKIE, SESSION_COOKIE_NAME + "=" + sessionId);
    }

    public static RequestSpecification withAuthData(String username, String password) {
        String sessionId = new CrudRequester(
                withLoginPass(username, password),
                EndpointRequests.GET_SESSION,
                ResponseSpecs.requestReturnsOk()
        )
                .GET()
                .extract()
                .cookie(SESSION_COOKIE_NAME);

        return withLoginPass(username, password)
                .cookie(SESSION_COOKIE_NAME, sessionId);
    }

    public static RequestSpecification withAdminAuth() {
        if (!sessionStorage.containsKey(ADMIN_USERNAME)) {
            sessionStorage.put(ADMIN_USERNAME, ADMIN_AUTH_SESSION_ID);
        }
        return basicRequestSpec().build()
                .headers(HttpHeaders.AUTHORIZATION, ADMIN_AUTH_BASIC).
                cookie(SESSION_COOKIE_NAME, sessionStorage.get(ADMIN_USERNAME));
    }

}
