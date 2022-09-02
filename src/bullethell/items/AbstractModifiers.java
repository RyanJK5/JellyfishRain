package bullethell.items;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.function.Function;

abstract class AbstractModifiers {

    public final void apply(Function<Float, Float> multFunction, Function<Integer, Integer> bonusFunction) {
        Field[] fields = getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!field.isAnnotationPresent(Modifier.class)) {
                continue;
            }

            try {
                if (field.getAnnotation(Modifier.class).isMultiplier()) {
                    field.setFloat(this, multFunction.apply(field.getFloat(this)));
                } else {
                    field.setInt(this, bonusFunction.apply(field.getInt(this)));
                }
            } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public final void apply(AbstractModifiers other) {
        if (!getClass().equals(other.getClass())) {
            return;
        }

        Field[] fields = getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!field.isAnnotationPresent(Modifier.class)) {
                continue;
            }

            try {
            if (field.getAnnotation(Modifier.class).isMultiplier()) {
                field.setFloat(this, field.getFloat(this) + other.getClass().getDeclaredField(field.getName()).getFloat(other));
            } else {
                field.setInt(this, field.getInt(this) + other.getClass().getDeclaredField(field.getName()).getInt(other));
            }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                    | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public final void reset() {
        apply(f -> 0f, i -> 0);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Modifier {
        boolean isMultiplier();
    }

    public final String toString() {
        String result = "";
        try {
            for (Field field : getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Modifier.class)) {
                    result += field.getName() + "=" + field.get(this) + "\n";
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }
}
