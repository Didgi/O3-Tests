package api;

import common.extensions.BugExtension;
import api.testdata.ReferenceTestData;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeFormatter;

@ExtendWith(BugExtension.class)
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
