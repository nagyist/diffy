package ai.diffy.flat;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ai.diffy.flat.FlatObject.TerminalFlatObject;

/**
 * Java port of the Scala FlatIndexedCollection and its associated condition types.
 */
public class FlatIndexedCollection {

    // reverseIndex: tokenizedPath -> { FlatObject -> List<TerminalFlatObject> }
    private final Map<List<TerminalFlatObject>, Map<FlatObject, List<TerminalFlatObject>>> reverseIndex
        = new LinkedHashMap<>();

    public void insert(FlatObject o) {
        for (Map.Entry<List<TerminalFlatObject>, List<TerminalFlatObject>> entry : o.tokenizedPaths().entrySet()) {
            reverseIndex
                .computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>())
                .put(o, entry.getValue());
        }
    }

    public List<FlatObject> collect(
            List<TerminalFlatObject> path,
            Predicate<TerminalFlatObject> predicate) {
        Map<FlatObject, List<TerminalFlatObject>> ovm = reverseIndex.get(path);
        if (ovm == null) return List.of();
        List<FlatObject> result = new ArrayList<>();
        for (Map.Entry<FlatObject, List<TerminalFlatObject>> entry : ovm.entrySet()) {
            for (TerminalFlatObject v : entry.getValue()) {
                if (predicate.test(v)) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // FlatCondition hierarchy
    // -------------------------------------------------------------------------

    public interface FlatCondition extends Predicate<FlatObject> {}

    public record Equals(FlatObject o) implements FlatCondition {
        @Override
        public boolean test(FlatObject other) { return o.equals(other); }
    }

    public record Matches(String regex) implements FlatCondition {
        @Override
        public boolean test(FlatObject other) {
            if (other instanceof FlatObject.FlatPrimitive<?> fp && fp.value() instanceof String s) {
                return s.matches(regex);
            }
            return false;
        }
    }

    public record TerminalCondition(
            List<TerminalFlatObject> path,
            FlatCondition predicate) implements FlatCondition {
        @Override
        public boolean test(FlatObject o) {
            return o.get(new ArrayList<>(path)).stream().anyMatch(predicate);
        }
    }

    public record MultipathCondition(
            ValueCondition predicate,
            List<List<TerminalFlatObject>> paths) implements FlatCondition {
        @Override
        public boolean test(FlatObject o) {
            List<List<FlatObject>> values = paths.stream()
                .map(p -> o.get(new ArrayList<>(p)))
                .collect(Collectors.toList());
            return predicate.test(values);
        }
    }

    // -------------------------------------------------------------------------
    // ValueCondition
    // -------------------------------------------------------------------------

    public interface ValueCondition extends Predicate<List<List<FlatObject>>> {}

    public static final ValueCondition veq = values -> {
        if (values.isEmpty() || values.size() == 1) return true;
        List<FlatObject> head = values.get(0);
        return values.stream().allMatch(v -> v.equals(head));
    };

    // -------------------------------------------------------------------------
    // Composite conditions
    // -------------------------------------------------------------------------

    public record And(FlatCondition... conditions) implements FlatCondition {
        @Override
        public boolean test(FlatObject o) {
            for (FlatCondition c : conditions) if (!c.test(o)) return false;
            return true;
        }
    }

    public record Or(FlatCondition... conditions) implements FlatCondition {
        @Override
        public boolean test(FlatObject o) {
            for (FlatCondition c : conditions) if (c.test(o)) return true;
            return false;
        }
    }
}
