package api.specs;

import api.config.Config;
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

import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public final class RequestSpecs {

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

}
