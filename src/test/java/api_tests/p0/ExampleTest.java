package api_tests.p0;

import api.testdata.PatientTestData;
import org.junit.jupiter.api.Test;

public class ExampleTest extends BaseApiTest {

    @Test
    void idgenTest() {
        PatientTestData.generateIdentifier();
    }
}
