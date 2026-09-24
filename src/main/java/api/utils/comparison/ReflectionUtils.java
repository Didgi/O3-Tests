package api.utils.comparison;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;

public class ReflectionUtils {

    public static Object getFieldValue(Object obj, String path) {
        if (obj == null || path == null) return null;

        String[] parts = path.split("\\.");
        Object current = obj;

        for (String part : parts) {
            current = getSingleField(current, part);
            if (current == null) return null;
        }

        return current;
    }

    // param: путь в ответе (patient.uuid); иначе то же имя или *Uuid -> *.uuid
    public static Object getResponseValue(Object response, String requestField, String responsePath) {
        if (responsePath != null && !responsePath.isBlank()) {
            return getFieldValue(response, responsePath);
        }

        Object direct = getFieldValue(response, requestField);
        if (direct != null) {
            Object nestedUuid = uuidFromObject(direct);
            return nestedUuid != null ? nestedUuid : direct;
        }

        String nestedUuidPath = toNestedUuidPath(requestField);
        if (nestedUuidPath != null) {
            return getFieldValue(response, nestedUuidPath);
        }

        return null;
    }

    private static String toNestedUuidPath(String field) {
        if (field != null && field.endsWith("Uuid") && field.length() > 4) {
            return field.substring(0, field.length() - 4) + ".uuid";
        }
        return null;
    }

    private static Object uuidFromObject(Object value) {
        if (value == null || value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return null;
        }
        return getFieldValue(value, "uuid");
    }

    private static Object getSingleField(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static boolean isEmpty(Object value) {
        if (value == null) return false;

        if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        }

        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }

        return false;
    }

    public static boolean isNotEmpty(Object value) {
        if (value == null) return false;

        if (value instanceof Collection<?>) {
            return !((Collection<?>) value).isEmpty();
        }

        if (value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }

        return false;
    }
}
