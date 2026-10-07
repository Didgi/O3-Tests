package ui.elements;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;

public class PatientAsideElement extends BaseElement {

    private static final String PATIENT_INFO_SLOT =
            "[data-extension-slot-name='patient-info-slot']";

    private static final By PATIENT_BANNER =
            byAttribute("data-extension-id", "patient-banner");

    private static final By VITALS_AND_BIOMETRICS =
            byAttribute("data-extension-id", "patient-vitals-info");

    public PatientAsideElement() {
        super($(PATIENT_INFO_SLOT).closest("aside"));
    }

    public PatientBannerElement getPatientBanner() {
        return new PatientBannerElement(
                find(PATIENT_BANNER)
        );
    }

    public VitalsAndBiometricsElement getVitalsAndBiometrics() {
        return new VitalsAndBiometricsElement(
                find(VITALS_AND_BIOMETRICS)
        );
    }
}
