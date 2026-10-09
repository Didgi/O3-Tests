package ui.pages;

import io.qameta.allure.Step;
import ui.elements.PatientAsideElement;

import static ui.pages.UiPath.PATIENT_CHART;

public class PatientChartPage extends BasePage<PatientChartPage> {

    private final String patientUuid;

    public PatientChartPage() {
        this.patientUuid = null;
    }

    public PatientChartPage(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @Override
    public String url() {
        if (patientUuid == null) {
            throw new IllegalStateException(
                    "Patient UUID is required to open Patient Chart directly"
            );
        }

        return PATIENT_CHART.formatted(patientUuid);
    }

    @Step("Проверяем, что открыта карточка пациента")
    public PatientChartPage checkPatientChartOpened() {
        getPatientAside()
                .getPatientBanner()
                .checkPatientBannerVisible();

        return this;
    }

    public PatientAsideElement getPatientAside() {
        return new PatientAsideElement();
    }
}