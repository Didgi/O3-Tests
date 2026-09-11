package api.utils;

import org.apache.commons.lang3.RandomStringUtils;

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

}
