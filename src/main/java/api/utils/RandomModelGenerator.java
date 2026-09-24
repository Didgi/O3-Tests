package api.utils;

import api.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.curiousoddman.rgxgen.RgxGen;
import com.github.curiousoddman.rgxgen.RgxGenBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RandomModelGenerator {

    private static final Random RANDOM = new Random();

    private RandomModelGenerator() {
    }

    public static <T> T generate(Class<T> clazz) {
        try {
            if (clazz.isRecord()) {
                return generateRecord(clazz);
            }

            return generatePojo(clazz);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate entity: " + clazz.getName(),
                    e
            );
        }
    }

    private static <T> T generateRecord(Class<T> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();

        Class<?>[] parameterTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        Object[] values = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];

            GeneratingRule rule = component.getAnnotation(GeneratingRule.class);
            if (rule == null) {
                Field field = clazz.getDeclaredField(component.getName());
                rule = field.getAnnotation(GeneratingRule.class);
            }

            values[i] = generateValue(
                    component.getType(),
                    component.getGenericType(),
                    rule,
                    component.getName()
            );
        }

        Constructor<T> constructor =
                clazz.getDeclaredConstructor(parameterTypes);

        constructor.setAccessible(true);

        return constructor.newInstance(values);
    }

    private static <T> T generatePojo(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);

        T instance = constructor.newInstance();

        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);

            GeneratingRule rule =
                    field.getAnnotation(GeneratingRule.class);

            Object value = generateValue(
                    field.getType(),
                    field.getGenericType(),
                    rule,
                    field.getName()
            );

            field.set(instance, value);
        }

        return instance;
    }

    private static Object generateValue(
            Class<?> type,
            Type genericType,
            GeneratingRule rule,
            String fieldName
    ) {
        if (rule != null) {
            if (rule.nullable()) {
                if (type.isPrimitive()) {
                    throw new IllegalArgumentException(
                            "Cannot set primitive field to null: " + fieldName
                    );
                }
                return null;
            }

            if (!rule.property().isBlank()) {
                return valueFromProperty(rule.property(), type);
            }

            if (!rule.regex().isBlank()) {
                return generateFromRegex(
                        rule.regex(),
                        type
                );
            }

            if (isBoolean(type)
                    && rule.booleanValue() != BooleanGeneration.RANDOM) {

                return rule.booleanValue() == BooleanGeneration.TRUE;
            }
        }

        return generateRandomValue(type, genericType);
    }

    private static Object valueFromProperty(String key, Class<?> type) {
        String value = Config.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Config property not found: " + key);
        }
        return castGenerated(value, type);
    }

    private static Object generateRandomValue(
            Class<?> type,
            Type genericType
    ) {
        if (type.equals(String.class)) {
            return UUID.randomUUID()
                    .toString()
                    .substring(0, 8);
        }

        if (type.equals(Integer.class) || type.equals(int.class)) {
            return RANDOM.nextInt(1000);
        }

        if (type.equals(Long.class) || type.equals(long.class)) {
            return RANDOM.nextLong();
        }

        if (type.equals(Double.class) || type.equals(double.class)) {
            return RANDOM.nextDouble() * 100;
        }

        if (isBoolean(type)) {
            return RANDOM.nextBoolean();
        }

        if (List.class.isAssignableFrom(type)) {
            return generateRandomList(genericType);
        }

        if (type.equals(Date.class)) {
            return new Date(
                    System.currentTimeMillis()
                            - RANDOM.nextInt(1_000_000_000)
            );
        }

        return generate(type);
    }

    private static Object generateFromRegex(
            String regex,
            Class<?> type
    ) {
        RgxGen rgxGen =
                new RgxGenBuilder(regex).parse();

        return castGenerated(rgxGen.generate(), type);
    }

    private static Object castGenerated(String value, Class<?> type) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        }

        if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(value);
        }

        if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        }

        if (isBoolean(type)) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    private static List<?> generateRandomList(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return Collections.emptyList();
        }

        Type actualType =
                parameterizedType.getActualTypeArguments()[0];

        if (actualType.equals(JsonNode.class)) {
            return Collections.emptyList();
        }

        if (actualType.equals(String.class)) {
            return List.of(
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 5),
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 5)
            );
        }

        if (actualType instanceof Class<?> elementClass) {
            return List.of(generate(elementClass));
        }

        return Collections.emptyList();
    }

    private static boolean isBoolean(Class<?> type) {
        return type.equals(boolean.class)
                || type.equals(Boolean.class);
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        Class<?> currentClass = clazz;

        while (currentClass != null
                && currentClass != Object.class) {

            fields.addAll(
                    Arrays.asList(currentClass.getDeclaredFields())
            );

            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }
}
