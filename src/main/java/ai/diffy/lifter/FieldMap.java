package ai.diffy.lifter;

import java.util.Map;
import java.util.TreeMap;

public class FieldMap {
    public final Map<String, Object> value;

    public FieldMap(Map<String, Object> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        // Match Scala: toSeq.sortBy key then toString
        return new TreeMap<>(value).toString();
    }
}
