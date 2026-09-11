package api.models.comparison;

import java.util.Objects;

public enum FieldRule {

    //сравнивает значения одинаковых полей по имени в запросе/ответе
    STANDARD_EQUALS((req, res, field, param) -> {
        Object r1 = ReflectionUtils.getFieldValue(req, field);
        Object r2 = ReflectionUtils.getFieldValue(res, field);
        return Objects.equals(r1, r2)
                ? null
                : new Mismatch(field, r1, r2);
    }),

    //проверяет, что поле должно существовать и не быть null
    REQUIRED((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return value != null
                ? null
                : new Mismatch(field, "not null", null);
    }),

    //проверяет, что поле должно существовать и быть null
    CHECK_NULL((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return value == null
                ? null
                : new Mismatch(field, null, value);
    }),

    //проверяет, что массив/список должен существовать и быть пустым
    ARRAY_EMPTY((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return ReflectionUtils.isEmpty(value)
                ? null
                : new Mismatch(field, "empty", value);
    }),

    //проверяет, что массив/список должен существовать и не быть пустым
    ARRAY_NOT_EMPTY((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return ReflectionUtils.isNotEmpty(value)
                ? null
                : new Mismatch(field, "not empty", value);
    }),

    //проверяет, что поле должно существовать и иметь значение true
    CHECK_TRUE((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return Boolean.TRUE.equals(value)
                ? null
                : new Mismatch(field, true, value);
    }),

    //проверяет, что поле должно существовать и иметь значение false
    CHECK_FALSE((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return Boolean.FALSE.equals(value)
                ? null
                : new Mismatch(field, false, value);
    }),

    //кастомное поле. Проверяет, что поле должно существовать и иметь значение больше указанного
    GREATER_THAN((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        if (value instanceof Number) {
            double actual = ((Number) value).doubleValue();
            double expected = Double.parseDouble(param);
            return actual > expected
                    ? null
                    : new Mismatch(field, ">" + expected, actual);
        }
        return new Mismatch(field, "number", value);
    }),

    //кастомное поле. Проверяет, что поле должно существовать и иметь значение меньше указанного
    LESS_THAN((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        if (value instanceof Number) {
            double actual = ((Number) value).doubleValue();
            double expected = Double.parseDouble(param);
            return actual < expected
                    ? null
                    : new Mismatch(field, "<" + expected, actual);
        }
        return new Mismatch(field, "number", value);
    }),

    //сравнивает значение поля в ответе со значением указанным в конфиге
    EQUALS((req, res, field, param) -> {
        Object value = ReflectionUtils.getFieldValue(res, field);
        return Objects.equals(String.valueOf(value), param)
                ? null
                : new Mismatch(field, param, value);
    });

    private final FieldCheck check;

    FieldRule(FieldCheck check) {
        this.check = check;
    }

    public Mismatch validate(Object req, Object res, String field, String param) {
        return check.apply(req, res, field, param);
    }

    @FunctionalInterface
    interface FieldCheck {
        Mismatch apply(Object req, Object res, String field, String param);
    }
}
