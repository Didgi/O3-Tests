package common;

import lombok.Getter;

@Getter
public enum Locations {
    INPATIENT_WARD("Inpatient Ward");

    final String value;

    Locations(String value) {
        this.value = value;
    }
}
