package api.utils;

import api.testdata.ReferenceTestData;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private static final int MIN_PAST_DAYS_AGO = 1;
    private static final int MAX_PAST_DAYS_AGO = 30;

    private RandomData() {
    }

    public static String randomString(int length) {
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

    public static LocalDate randomFutureDate() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);
        long randomEpochDay = ThreadLocalRandom.current().nextLong(start.toEpochDay(), end.toEpochDay() + 1);
        return LocalDate.ofEpochDay(randomEpochDay);
    }

    /**
     * Generates a timestamp on one of the previous 30 days, excluding today,
     * in the configured time zone with second precision.
     */
    public static OffsetDateTime randomPastDateTime() {
        int daysAgo = ThreadLocalRandom.current()
                .nextInt(MIN_PAST_DAYS_AGO, MAX_PAST_DAYS_AGO + 1);

        return ZonedDateTime.now(ZoneId.of(ReferenceTestData.timeZone()))
                .minusDays(daysAgo)
                .truncatedTo(ChronoUnit.SECONDS)
                .toOffsetDateTime();
    }

    public static String generateRandomTime() {
        int hour = ThreadLocalRandom.current().nextInt(9, 17);
        int minute = ThreadLocalRandom.current().nextInt(0, 6) * 10;

        int hour12 = hour > 12 ? hour - 12 : hour;

        return String.format("%02d:%02d", hour12, minute);
    }

    public static String generateRandomDuration() {
        return String.valueOf((new Random().nextInt(12) + 1) * 10);
    }

    public static LocalTime to24HourTime(String time, String period) {
        return LocalTime.parse(
                time + " " + period,
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        );
    }
}
