package api_tests.p0;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseApiTest {
    public SoftAssertions softly;

    @BeforeEach
    public void setUp() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void cleanUp() {
        softly.assertAll();
    }
}
