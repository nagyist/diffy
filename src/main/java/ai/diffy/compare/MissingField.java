package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;

public enum MissingField implements TerminalDifference {
    INSTANCE;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "MissingField");
        m.put("left", "present");
        m.put("right", "nil");
        return m;
    }

    @Override
    public Map<String, Difference> flattened() {
        return Map.of("MissingField", this);
    }
}
