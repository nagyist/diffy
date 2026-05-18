package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record ObjectDifference(MapDifference<String> mapDiff) implements Difference {

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Difference> flattened() {
        Map<String, Difference> result = new LinkedHashMap<>();

        // existing keys: for each (key, diff) in values, prefix flattened entries with "key."
        for (Map.Entry<String, Difference> entry : mapDiff.values().entrySet()) {
            String key = entry.getKey();
            for (Map.Entry<String, Difference> inner : entry.getValue().flattened().entrySet()) {
                result.put(key + "." + inner.getKey(), inner.getValue());
            }
        }

        // missing / extra fields from the keys diff
        if (mapDiff.keys() instanceof SetDifference<?> sd) {
            Set<String> leftNotRight = (Set<String>) sd.leftNotRight();
            Set<String> rightNotLeft = (Set<String>) sd.rightNotLeft();
            for (String x : leftNotRight) {
                result.put(x + ".MissingField", MissingField.INSTANCE);
            }
            for (String x : rightNotLeft) {
                result.put(x + ".ExtraField", ExtraField.INSTANCE);
            }
        }
        // NoDifference on keys → no missing/extra entries

        return result;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "ObjectDifference");
        Map<String, Object> children = new LinkedHashMap<>();
        mapDiff.values().forEach((k, d) -> children.put(k, d.toMap()));
        m.put("children", children);
        return m;
    }
}
