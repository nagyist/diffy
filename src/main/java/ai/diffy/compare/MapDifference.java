package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;

public record MapDifference<A>(TerminalDifference keys, Map<A, Difference> values)
        implements TerminalDifference {

    @Override
    public Map<String, Difference> flattened() {
        Map<String, Difference> result = new LinkedHashMap<>();
        // keys part: prefix each entry with "keys."
        for (Map.Entry<String, Difference> e : keys.flattened().entrySet()) {
            result.put("keys." + e.getKey(), e.getValue());
        }
        // values part: prefix each entry with "values."
        for (Difference vd : values.values()) {
            for (Map.Entry<String, Difference> e : vd.flattened().entrySet()) {
                result.put("values." + e.getKey(), e.getValue());
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "MapDifference");
        m.put("keys", keys.toMap());
        Map<Object, Object> vMap = new LinkedHashMap<>();
        values.forEach((k, d) -> vMap.put(k, d.toMap()));
        m.put("values", vMap);
        return m;
    }
}
