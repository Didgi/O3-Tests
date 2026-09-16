package api.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;

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

    public static LocalDateTime startDate() {
        return LocalDateTime.now().plusDays(14);
    }

    public static LocalDateTime endDate() {
        return startDate().plusMinutes(30);
    }

}
