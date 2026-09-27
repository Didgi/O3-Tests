package api.config;

import lombok.Getter;

@Getter
public enum Roles {

    SUPER_ADMIN("System developer"),
    APP_CONFIG_FORMS("Application: Configures Forms");

    final String value;

    Roles(String value) {
        this.value = value;
    }

}