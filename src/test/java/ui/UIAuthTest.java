package ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UIAuthTest extends UIBaseTest {

    @Test
    @DisplayName("Позитивный тест: пользователь может изменить имя на другое валидное")
    public void userCanChangeHisNameWithValidData() {


        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened();
    }
}
