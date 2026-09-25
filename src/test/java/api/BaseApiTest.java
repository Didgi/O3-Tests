package api;

import api.requests.steps.ApiClient;
import api.testdata.ReferenceTestData;
import common.extensions.BugExtension;
import common.extensions.UserExtensions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.format.DateTimeFormatter;

@ExtendWith(BugExtension.class)
@ExtendWith(UserExtensions.class)
public class BaseApiTest {
    public SoftAssertions softly;
    protected static final String TIME_ZONE = ReferenceTestData.timeZone();
    protected static final DateTimeFormatter ON_DATE_FORMAT =
            DateTimeFormatter.ofPattern(ReferenceTestData.dateTimeFormat());

    protected ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @BeforeEach
    public void setupSoftly() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void assertSoftly() {
        softly.assertAll();
    }
}
