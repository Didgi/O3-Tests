package api_tests;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

//@ExtendWith(BugExtension.class)
//@ExtendWith(TimingExtension.class)
//@ExtendWith(ApiVersionExtension.class)
//@ExtendWith(FraudCheckExtension.class)
public class BaseTestSenior {
    protected String authUserToken;
    protected int userAccount;
    public SoftAssertions softly;

    @BeforeEach
    public void setUp() {
        softly = new SoftAssertions();
//        UserSteps.SoftAssertions(softly);
//        authUserToken = createUserAndGetToken();
//        userAccount = createUserAccount(authUserToken);
    }

    @AfterEach
    public void cleanUp() {
//        deleteUsersById();
        softly.assertAll();
    }
}
