package api_tests.p0;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseApiTest {
    public SoftAssertions softly;

    @BeforeEach
    public void setupSoftly() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void assertSoftly() {
        softly.assertAll();
    }
}
