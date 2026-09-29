package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Getter
@NoArgsConstructor
public class ServiceQueuesPage extends BasePage<ServiceQueuesPage> {

    private final SelenideElement mainTitle = $(Selectors.byText("Service queues"));

    @Override
    public String url() {
        return UiPath.SERVICE_QUEUES;
    }

    @Step("Проверяем, что открыта страница Service Queues")
    public ServiceQueuesPage checkServiceQueuesOpened(){
        mainTitle.shouldBe(visible);
        return this;
    }
}
