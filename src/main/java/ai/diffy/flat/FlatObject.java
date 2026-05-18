package ai.diffy.flat;

import ai.diffy.lifter.FieldMap;
import ai.diffy.lifter.JsonLifter;
import ai.diffy.util.Memoize;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Java port of the Scala FlatObject sealed trait hierarchy.
 *
 * Not sealed here because FlatDifference extends FlatObject from a different file
 * (matching the Scala design where FlatDifference extends FlatObject).
 *
 * Hierarchy:
 *   FlatObject (interface)
 *     TerminalFlatObject (interface)
 *       FlatNull (singleton enum)
 *       FlatPrimitive<T> (record)
 *     FlatCollection<T> (abstract class)
 *       FlatSeq<T>
 *       FlatSet<T>
 *     FlatMap<K,V>
 *     FlatStruct
 */
public interface FlatObject {

    Map<FlatObject, FlatObject> normalized();

    default FlatObject flatMap(java.util.function.BiFunction<FlatObject, FlatObject, FlatObject> f) {
        Map<FlatObject, FlatObject> result = new LinkedHashMap<>();
        for (Map.Entry<FlatObject, FlatObject> e : normalized().entrySet()) {
            result.putAll(f.apply(e.getKey(), e.getValue()).normalized());
        }
        return new FlatMap<>(result);
    }

    default List<FlatObject> get(FlatObject key) {
        if (key instanceof FlatNull) {
            return new ArrayList<>(normalized().values());
        }
        FlatObject val = normalized().get(key);
        return val != null ? List.of(val) : List.of();
    }

    default List<FlatObject> get(List<FlatObject> path) {
        if (path.isEmpty()) return List.of(this);
        List<FlatObject> result = new ArrayList<>();
        for (FlatObject fo : get(path.get(0))) {
            result.addAll(fo.get(path.subList(1, path.size())));
        }
        return result;
    }

    default List<FlatObject> getMatching(List<TerminalFlatObject> tokenizedPath) {
        if (tokenizedPath.isEmpty()) return List.of(this);
        if (tokenizedPath.get(0) instanceof FlatNull) {
            List<TerminalFlatObject> tail = tokenizedPath.subList(1, tokenizedPath.size());
            List<FlatObject> result = new ArrayList<>();
            for (FlatObject v : normalized().values()) {
                result.addAll(v.getMatching(tail));
            }
            return result;
        }
        List<FlatObject> result = new ArrayList<>();
        List<TerminalFlatObject> tail = tokenizedPath.subList(1, tokenizedPath.size());
        for (FlatObject fo : get(tokenizedPath.get(0))) {
            result.addAll(fo.getMatching(tail));
        }
        return result;
    }

    default Map<List<FlatObject>, TerminalFlatObject> flatTable() {
        Map<List<FlatObject>, TerminalFlatObject> result = new LinkedHashMap<>();
        for (Map.Entry<FlatObject, FlatObject> entry : normalized().entrySet()) {
            FlatObject pkey = entry.getKey();
            FlatObject value = entry.getValue();
            for (Map.Entry<List<FlatObject>, TerminalFlatObject> inner : value.flatTable().entrySet()) {
                List<FlatObject> fullPath = new ArrayList<>();
                fullPath.add(pkey);
                fullPath.addAll(inner.getKey());
                result.put(fullPath, inner.getValue());
            }
        }
        return result;
    }

    default List<String> paths() {
        return flatTable().keySet().stream()
            .map(path -> path.stream()
                .map(fo -> fo instanceof FlatPrimitive<?> fp && fp.value() instanceof String s ? s : "$")
                .collect(Collectors.joining(".")))
            .collect(Collectors.toList());
    }

    default Map<List<TerminalFlatObject>, List<TerminalFlatObject>> tokenizedPaths() {
        Map<List<TerminalFlatObject>, List<TerminalFlatObject>> acc = new LinkedHashMap<>();
        for (Map.Entry<List<FlatObject>, TerminalFlatObject> entry : flatTable().entrySet()) {
            List<TerminalFlatObject> tokenizedPath = entry.getKey().stream()
                .map(fo -> (fo instanceof FlatPrimitive<?> fp && fp.value() instanceof String)
                    ? (TerminalFlatObject) fp
                    : FlatNull.INSTANCE)
                .collect(Collectors.toList());
            acc.computeIfAbsent(tokenizedPath, k -> new ArrayList<>()).add(entry.getValue());
        }
        return acc;
    }

    default List<FlatEntry> rendered() {
        List<FlatEntry> result = new ArrayList<>();
        for (Map.Entry<List<FlatObject>, TerminalFlatObject> entry : flatTable().entrySet()) {
            String key = entry.getKey().stream()
                .map(fo -> fo instanceof FlatPrimitive<?> fp ? fp.value().toString() : "$")
                .collect(Collectors.joining("."));
            result.add(new FlatEntry(key, entry.getValue().token()));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // TerminalFlatObject
    // -------------------------------------------------------------------------
    interface TerminalFlatObject extends FlatObject {
        @Override
        default Map<FlatObject, FlatObject> normalized() { return Map.of(); }

        @Override
        default Map<List<FlatObject>, TerminalFlatObject> flatTable() {
            return Map.of(List.of(), this);
        }

        default String token() { return "$"; }
    }

    // -------------------------------------------------------------------------
    // FlatNull singleton
    // -------------------------------------------------------------------------
    enum FlatNull implements TerminalFlatObject {
        INSTANCE;
    }

    // -------------------------------------------------------------------------
    // FlatPrimitive<T>
    // -------------------------------------------------------------------------
    record FlatPrimitive<T>(T value) implements TerminalFlatObject {
        @Override
        public String token() { return value != null ? value.toString() : "null"; }
    }

    // -------------------------------------------------------------------------
    // FlatCollection (abstract)
    // -------------------------------------------------------------------------
    abstract class FlatCollection<T extends FlatObject> implements FlatObject {
        protected abstract Iterable<T> items();

        private volatile Map<FlatObject, FlatObject> _normalized;

        @Override
        public Map<FlatObject, FlatObject> normalized() {
            if (_normalized == null) {
                synchronized (this) {
                    if (_normalized == null) {
                        Map<FlatObject, FlatObject> m = new LinkedHashMap<>();
                        int i = 0;
                        for (T v : items()) {
                            m.put(new FlatPrimitive<>(i++), v);
                        }
                        _normalized = m;
                    }
                }
            }
            return _normalized;
        }
    }

    // -------------------------------------------------------------------------
    // FlatSeq<T>
    // -------------------------------------------------------------------------
    final class FlatSeq<T extends FlatObject> extends FlatCollection<T> {
        private final List<T> value;

        public FlatSeq(List<T> value) { this.value = value; }

        public List<T> value() { return value; }

        @Override
        protected Iterable<T> items() { return value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FlatSeq<?> other)) return false;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() { return Objects.hash(value); }

        @Override
        public String toString() { return "FlatSeq(" + value + ")"; }
    }

    // -------------------------------------------------------------------------
    // FlatSet<T>
    // -------------------------------------------------------------------------
    final class FlatSet<T extends FlatObject> extends FlatCollection<T> {
        private final Set<T> value;
        private final List<T> sorted;

        public FlatSet(Set<T> value) {
            this.value = value;
            this.sorted = value.stream()
                .sorted(Comparator.comparing(Object::toString))
                .collect(Collectors.toList());
        }

        public Set<T> value() { return value; }

        @Override
        protected Iterable<T> items() { return sorted; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FlatSet<?> other)) return false;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() { return Objects.hash(value); }

        @Override
        public String toString() { return "FlatSet(" + value + ")"; }
    }

    // -------------------------------------------------------------------------
    // FlatMap<K,V>
    // -------------------------------------------------------------------------
    record FlatMap<K extends FlatObject, V extends FlatObject>(Map<K, V> value) implements FlatObject {
        @SuppressWarnings("unchecked")
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return (Map<FlatObject, FlatObject>) (Map<?, ?>) value;
        }
    }

    // -------------------------------------------------------------------------
    // FlatStruct
    // -------------------------------------------------------------------------
    record FlatStruct(FlatMap<FlatPrimitive<String>, FlatObject> value) implements FlatObject {
        @SuppressWarnings("unchecked")
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return (Map<FlatObject, FlatObject>) (Map<?, ?>) value.value();
        }
    }

    // =========================================================================
    // Static factory / reflection helpers
    // =========================================================================

    Function<Class<?>, Function<Object, Map<String, Object>>> _flatMapMaker =
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

    private static Map<String, Object> mkMap(Object obj) {
        return FlatObjectMapMaker.INSTANCE.apply(obj.getClass()).apply(obj);
    }

    static boolean isPrimitive(Object a) {
        return a instanceof Boolean
            || a instanceof Byte
            || a instanceof Character
            || a instanceof Short
            || a instanceof Integer
            || a instanceof Long
            || a instanceof Float
            || a instanceof Double
            || a instanceof String;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static FlatObject lift(Object a) {
        if (a instanceof FlatObject fo) return fo;
        if (isPrimitive(a)) return new FlatPrimitive<>(a);
        if (a instanceof List<?> list) {
            List<FlatObject> lifted = list.stream().map(FlatObject::lift).collect(Collectors.toList());
            return new FlatSeq<>(lifted);
        }
        if (a instanceof Set<?> set) {
            Set<FlatObject> lifted = set.stream().map(FlatObject::lift)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            return new FlatSet<>(lifted);
        }
        if (a instanceof Map<?, ?> map) {
            Map<FlatObject, FlatObject> lifted = new LinkedHashMap<>();
            map.forEach((k, v) -> lifted.put(lift(k), lift(v)));
            return new FlatMap<>(lifted);
        }
        if (a != null && a.getClass().isArray()) {
            if (a instanceof Object[] arr) return lift(Arrays.asList(arr));
        }
        if (a instanceof ByteBuffer bb) return lift(new String(bb.asReadOnlyBuffer().array()));
        if (a instanceof com.fasterxml.jackson.databind.JsonNode jn) return lift(JsonLifter.lift(jn));
        if (a == null) return FlatNull.INSTANCE;
        if (a instanceof Enum<?> e) return new FlatPrimitive<>(e.name());
        if (a instanceof FieldMap fm) {
            FlatObject liftedMap = lift(fm.value);
            if (liftedMap instanceof FlatMap<?, ?> fmap) {
                return new FlatStruct((FlatMap<FlatPrimitive<String>, FlatObject>) fmap);
            }
            return FlatNull.INSTANCE;
        }
        // Arbitrary object: reflect fields
        Map<String, Object> fields = mkMap(a);
        Map<FlatPrimitive<String>, FlatObject> lifted = new LinkedHashMap<>();
        fields.forEach((k, v) -> lifted.put(new FlatPrimitive<>(k), lift(v)));
        return new FlatStruct(new FlatMap<>(lifted));
    }
}
