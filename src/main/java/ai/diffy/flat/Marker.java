package ai.diffy.flat;

import java.util.Set;

import static ai.diffy.flat.FlatObject.FlatPrimitive;

public class Marker {
    public static final FlatPrimitive<String> Left    = new FlatPrimitive<>("sn126left");
    public static final FlatPrimitive<String> Right   = new FlatPrimitive<>("sn126right");
    public static final FlatPrimitive<String> Value   = new FlatPrimitive<>("sn126value");
    public static final FlatPrimitive<String> Field   = new FlatPrimitive<>("sn126field");
    public static final FlatPrimitive<String> Missing = new FlatPrimitive<>("sn126missing");
    public static final FlatPrimitive<String> Extra   = new FlatPrimitive<>("sn126extra");
    public static final FlatPrimitive<String> Key     = new FlatPrimitive<>("sn126key");

    public static final Set<FlatPrimitive<String>> all          = Set.of(Left, Right, Value, Field, Missing, Extra, Key);
    public static final Set<FlatPrimitive<String>> leafMarkers  = Set.of(Left, Right, Missing, Extra);
    public static final Set<FlatPrimitive<String>> containerMarkers = Set.of(Key, Value);

    public static boolean isOne(FlatObject flatObject) { return all.contains(flatObject); }
    public static boolean isLeaf(FlatObject flatObject) { return leafMarkers.contains(flatObject); }
    public static boolean isContainer(FlatObject flatObject) { return containerMarkers.contains(flatObject); }
}
