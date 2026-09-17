package api.utils.comparison;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

final class DateTimeComparison {

    private static final DateTimeFormatter OFFSET_WITHOUT_COLON = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendOffset("+HHMM", "Z")
            .toFormatter();

    private DateTimeComparison() {
    }

    static boolean equalsTruncatedToSeconds(Object expected, Object actual) {
        Instant right = parseInstant(actual);
        if (right == null) {
            return Objects.equals(expected, actual);
        }

        Instant rightSeconds = right.truncatedTo(ChronoUnit.SECONDS);
        for (Instant left : parseCandidates(expected)) {
            if (left.truncatedTo(ChronoUnit.SECONDS).equals(rightSeconds)) {
                return true;
            }
        }
        return false;
    }

    static Instant parseInstant(Object value) {
        Set<Instant> candidates = parseCandidates(value);
        return candidates.isEmpty() ? null : candidates.iterator().next();
    }

    private static Set<Instant> parseCandidates(Object value) {
        Set<Instant> candidates = new LinkedHashSet<>();
        if (value == null) {
            return candidates;
        }
        if (value instanceof Instant instant) {
            candidates.add(instant);
            return candidates;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            candidates.add(offsetDateTime.toInstant());
            return candidates;
        }
        if (value instanceof LocalDateTime localDateTime) {
            addZonedCandidates(candidates, localDateTime);
            return candidates;
        }
        if (value instanceof Number number) {
            candidates.add(fromEpochNumber(number.longValue()));
            return candidates;
        }

        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return candidates;
        }
        if (text.chars().allMatch(Character::isDigit)) {
            candidates.add(fromEpochNumber(Long.parseLong(text)));
            return candidates;
        }

        Instant instant = tryParseInstant(text);
        if (instant != null) {
            candidates.add(instant);
            return candidates;
        }

        LocalDateTime localDateTime = tryParseLocalDateTime(text);
        if (localDateTime != null) {
            addZonedCandidates(candidates, localDateTime);
        }
        return candidates;
    }

    private static void addZonedCandidates(Set<Instant> candidates, LocalDateTime localDateTime) {
        candidates.add(localDateTime.toInstant(ZoneOffset.UTC));
        candidates.add(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static Instant tryParseInstant(String text) {
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(text, OFFSET_WITHOUT_COLON).toInstant();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static LocalDateTime tryParseLocalDateTime(String text) {
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static Instant fromEpochNumber(long value) {
        if (value >= 100_000_000_000L) {
            return Instant.ofEpochMilli(value);
        }
        return Instant.ofEpochSecond(value);
    }
}
