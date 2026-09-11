package api.models.comparison;

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
