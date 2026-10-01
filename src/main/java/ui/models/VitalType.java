package ui.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VitalType {

    BP("BP"),
    HEART_RATE("Heart rate"),
    RESPIRATORY_RATE("R. rate"),
    SPO2("SpO2"),
    TEMPERATURE("Temp"),
    WEIGHT("Weight"),
    HEIGHT("Height"),
    BMI("BMI");

    private final String displayName;
}
