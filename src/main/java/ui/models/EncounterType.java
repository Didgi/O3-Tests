package ui.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EncounterType {

    VITALS("Vitals"),
    CONSULTATION("Consultation");

    private final String displayName;
}
