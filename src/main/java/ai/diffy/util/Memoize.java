package ai.diffy.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Memoize {
    public static <A, B> Function<A, B> apply(Function<A, B> function) {
        ConcurrentHashMap<A, B> map = new ConcurrentHashMap<>();
        return (A a) -> map.computeIfAbsent(a, function);
    }
}
