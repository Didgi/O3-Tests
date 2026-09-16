package api;

import api.testdata.ReferenceTestData;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.format.DateTimeFormatter;

public class BaseApiTest {
    public SoftAssertions softly;
    protected static final String TIME_ZONE = ReferenceTestData.timeZone();
    protected static final DateTimeFormatter ON_DATE_FORMAT =
            DateTimeFormatter.ofPattern(ReferenceTestData.dateTimeFormat());

    @BeforeEach
    public void setupSoftly() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void assertSoftly() {
        softly.assertAll();
    }
}
