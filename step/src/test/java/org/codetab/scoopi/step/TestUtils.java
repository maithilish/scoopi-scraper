package org.codetab.scoopi.step;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestUtils {

    private TestUtils() {
    }

    public static void setFinalStaticField(final Class<?> clazz,
            final String fieldName, final Object value)
            throws ReflectiveOperationException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, value);
    }
}
