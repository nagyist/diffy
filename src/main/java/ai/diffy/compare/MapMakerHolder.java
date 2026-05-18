package ai.diffy.compare;

import ai.diffy.util.Memoize;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Memoized per-class reflection accessor used by Difference.mkMap().
 */
public class MapMakerHolder {
    public static final Function<Class<?>, Function<Object, Map<String, Object>>> INSTANCE =
        Memoize.apply(c -> {
            Field[] fields = Arrays.stream(c.getDeclaredFields())
                .filter(f -> !f.getName().contains("$"))
                .toArray(Field[]::new);
            for (Field f : fields) f.setAccessible(true);
            return obj -> {
                Map<String, Object> m = new LinkedHashMap<>();
                for (Field f : fields) {
                    try { m.put(f.getName(), f.get(obj)); } catch (IllegalAccessException ignored) {}
                }
                return m;
            };
        });
}
