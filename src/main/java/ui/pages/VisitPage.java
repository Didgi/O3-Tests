package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.codeborne.selenide.Selenide.$;
import static ui.pages.UiPath.PATIENT;

@Getter
public class VisitPage extends BasePage<VisitPage> {
    private final String patientUuid;
    private final SelenideElement visitTab = $(Selectors.byTagAndText("span", "Visits"));

    public VisitPage(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @Override
    public String url() {
        return PATIENT.formatted(patientUuid) + "/visits";
    }
}
