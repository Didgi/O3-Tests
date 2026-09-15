package api.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.util.UUID;

public class RandomData {
    private RandomData() {
    }

    public static String randomName(int length) {
        final String randomed = RandomStringUtils.secure().nextAlphabetic(length).toLowerCase();
        return randomed + " " + randomed;
    }

    public static String randomInvalidName(int length) {
        return RandomStringUtils.secure().nextAlphabetic(length).toLowerCase();
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    public static String uniqueValue(String prefix) {
        return prefix + uniqueSuffix();
    }

    public static String futureDate() {
        return LocalDate.now()
                .plusYears(1)
                .toString();
    }

    public static String pastDateYears(int years) {
        return LocalDate.now()
                .minusYears(years)
                .toString();
    }

    public static String changeLastCharacter(String value) {
        String replacement = value.endsWith("0") ? "1" : "0";

        return value.substring(0, value.length() - 1)
                + replacement;
    }
}
