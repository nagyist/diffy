package ai.diffy.flat;

import java.util.*;
import java.util.stream.Collectors;

import static ai.diffy.flat.FlatObject.*;

/**
 * Java port of the Scala FlatDifference ADT.
 */
public sealed interface FlatDifference extends FlatObject
    permits FlatDifference.TerminalFlatDifference, FlatDifference.IndexedFlatDifference,
            FlatDifference.ObjectFlatDifference, FlatDifference.SeqFlatDifference {

    // -------------------------------------------------------------------------
    // Terminal base
    // -------------------------------------------------------------------------
    sealed interface TerminalFlatDifference extends FlatDifference
        permits FlatDifference.NoFlatDifference, FlatDifference.TypeFlatDifference,
                FlatDifference.PrimitiveFlatDifference,
                FlatDifference.MissingField, FlatDifference.ExtraField,
                FlatDifference.OrderingFlatDifference, FlatDifference.SeqSizeFlatDifference,
                FlatDifference.SetFlatDifference, FlatDifference.MapFlatDifference {}

    // -------------------------------------------------------------------------
    // NoFlatDifference
    // -------------------------------------------------------------------------
    record NoFlatDifference<A>(A value) implements TerminalFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() { return Map.of(); }
    }

    // -------------------------------------------------------------------------
    // TypeFlatDifference
    // -------------------------------------------------------------------------
    record TypeFlatDifference<A, B>(A left, B right) implements TerminalFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(
                Marker.Left, new FlatPrimitive<>(left.getClass().getSimpleName()),
                Marker.Right, new FlatPrimitive<>(right.getClass().getSimpleName())
            );
        }
    }

    // -------------------------------------------------------------------------
    // PrimitiveFlatDifference
    // -------------------------------------------------------------------------
    record PrimitiveFlatDifference<A>(FlatPrimitive<A> left, FlatPrimitive<A> right)
            implements TerminalFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(Marker.Left, left, Marker.Right, right);
        }
    }

    // -------------------------------------------------------------------------
    // MissingField singleton
    // -------------------------------------------------------------------------
    enum MissingField implements TerminalFlatDifference {
        INSTANCE;
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(Marker.Field, Marker.Missing);
        }
    }

    // -------------------------------------------------------------------------
    // ExtraField singleton
    // -------------------------------------------------------------------------
    enum ExtraField implements TerminalFlatDifference {
        INSTANCE;
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(Marker.Field, Marker.Extra);
        }
    }

    // -------------------------------------------------------------------------
    // SeqFlatDifference marker
    // -------------------------------------------------------------------------
    sealed interface SeqFlatDifference extends FlatDifference
        permits FlatDifference.OrderingFlatDifference, FlatDifference.SeqSizeFlatDifference,
                FlatDifference.IndexedFlatDifference {}

    // -------------------------------------------------------------------------
    // OrderingFlatDifference
    // -------------------------------------------------------------------------
    record OrderingFlatDifference(List<Integer> leftPattern, List<Integer> rightPattern)
            implements TerminalFlatDifference, SeqFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(
                Marker.Left, FlatObject.lift(leftPattern),
                Marker.Right, FlatObject.lift(rightPattern)
            );
        }
    }

    // -------------------------------------------------------------------------
    // SeqSizeFlatDifference
    // -------------------------------------------------------------------------
    record SeqSizeFlatDifference<A>(List<A> leftNotRight, List<A> rightNotLeft)
            implements TerminalFlatDifference, SeqFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(
                Marker.Left, FlatObject.lift(leftNotRight),
                Marker.Right, FlatObject.lift(rightNotLeft)
            );
        }
    }

    // -------------------------------------------------------------------------
    // IndexedFlatDifference
    // -------------------------------------------------------------------------
    record IndexedFlatDifference(List<FlatDifference> indexedDiffs)
            implements FlatDifference, SeqFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            Map<FlatObject, FlatObject> result = new LinkedHashMap<>();
            for (FlatDifference d : indexedDiffs) {
                result.putAll(d.normalized());
            }
            return result;
        }
    }

    // -------------------------------------------------------------------------
    // SetFlatDifference
    // -------------------------------------------------------------------------
    record SetFlatDifference<A>(Set<A> leftNotRight, Set<A> rightNotLeft)
            implements TerminalFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(
                Marker.Left, FlatObject.lift(leftNotRight),
                Marker.Right, FlatObject.lift(rightNotLeft)
            );
        }
    }

    // -------------------------------------------------------------------------
    // MapFlatDifference
    // -------------------------------------------------------------------------
    record MapFlatDifference<A>(TerminalFlatDifference keys, Map<A, FlatDifference> values)
            implements TerminalFlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return Map.of(
                Marker.Key, keys,
                Marker.Value, FlatObject.lift(values)
            );
        }
    }

    // -------------------------------------------------------------------------
    // ObjectFlatDifference
    // -------------------------------------------------------------------------
    record ObjectFlatDifference(MapFlatDifference<FlatObject> mapDiff) implements FlatDifference {
        @Override
        public Map<FlatObject, FlatObject> normalized() {
            return mapDiff.normalized();
        }
    }

    // =========================================================================
    // Static factory
    // =========================================================================

    @SuppressWarnings({"unchecked", "rawtypes"})
    static FlatDifference apply(FlatObject left, FlatObject right) {
        if (left.equals(right)) return new NoFlatDifference<>(left);
        if (left instanceof FlatPrimitive<?> lp && right instanceof FlatPrimitive<?> rp)
            return new PrimitiveFlatDifference(lp, rp);
        if (left instanceof FlatSeq<?> ls && right instanceof FlatSeq<?> rs)
            return diffSeq((FlatSeq<FlatObject>) ls, (FlatSeq<FlatObject>) rs);
        if (left instanceof FlatSet<?> lset && right instanceof FlatSet<?> rset)
            return diffSet((FlatSet<FlatObject>) lset, (FlatSet<FlatObject>) rset);
        if (left instanceof FlatStruct ls && right instanceof FlatStruct rs)
            return diffObjectMap(ls, rs);
        if (left instanceof FlatMap<?, ?> lm && right instanceof FlatMap<?, ?> rm)
            return diffMap((FlatMap<FlatObject, FlatObject>) lm, (FlatMap<FlatObject, FlatObject>) rm);
        return new TypeFlatDifference<>(left, right);
    }

    static TerminalFlatDifference diffSet(FlatSet<FlatObject> left, FlatSet<FlatObject> right) {
        if (left.equals(right)) return new NoFlatDifference<>(left);
        Set<FlatObject> lnr = new HashSet<>(left.value());
        lnr.removeAll(right.value());
        Set<FlatObject> rnl = new HashSet<>(right.value());
        rnl.removeAll(left.value());
        return new SetFlatDifference<>(lnr, rnl);
    }

    static SeqFlatDifference diffSeq(FlatSeq<FlatObject> left, FlatSeq<FlatObject> right) {
        List<FlatObject> leftNotRight = new ArrayList<>(left.value());
        leftNotRight.removeAll(right.value());
        List<FlatObject> rightNotLeft = new ArrayList<>(right.value());
        rightNotLeft.removeAll(left.value());

        if (leftNotRight.isEmpty() && rightNotLeft.isEmpty()) {
            List<Integer> lPattern = left.value().stream()
                .map(e -> left.value().indexOf(e))
                .collect(Collectors.toList());
            List<Integer> rPattern = right.value().stream()
                .map(e -> left.value().indexOf(e))
                .collect(Collectors.toList());
            return new OrderingFlatDifference(lPattern, rPattern);
        } else if (left.value().size() == right.value().size()) {
            List<FlatDifference> diffs = new ArrayList<>();
            for (int i = 0; i < left.value().size(); i++) {
                diffs.add(apply(FlatObject.lift(left.value().get(i)), FlatObject.lift(right.value().get(i))));
            }
            return new IndexedFlatDifference(diffs);
        } else {
            return new SeqSizeFlatDifference<>(leftNotRight, rightNotLeft);
        }
    }

    static MapFlatDifference<FlatObject> diffMap(FlatMap<FlatObject, FlatObject> lm, FlatMap<FlatObject, FlatObject> rm) {
        FlatSet<FlatObject> lKeys = new FlatSet<>(lm.value().keySet());
        FlatSet<FlatObject> rKeys = new FlatSet<>(rm.value().keySet());
        TerminalFlatDifference keysDiff = diffSet(lKeys, rKeys);

        Set<FlatObject> shared = new HashSet<>(lm.value().keySet());
        shared.retainAll(rm.value().keySet());
        Map<FlatObject, FlatDifference> valueDiffs = new LinkedHashMap<>();
        for (FlatObject key : shared) {
            valueDiffs.put(key, apply(lm.value().get(key), rm.value().get(key)));
        }
        return new MapFlatDifference<>(keysDiff, valueDiffs);
    }

    @SuppressWarnings("unchecked")
    static ObjectFlatDifference diffObjectMap(FlatStruct lm, FlatStruct rm) {
        return new ObjectFlatDifference(diffMap(
            (FlatMap<FlatObject, FlatObject>) (FlatMap<?, ?>) lm.value(),
            (FlatMap<FlatObject, FlatObject>) (FlatMap<?, ?>) rm.value()
        ));
    }
}
