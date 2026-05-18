package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ExtraField implements TerminalDifference {
    INSTANCE;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "ExtraField");
        m.put("left", "nil");
        m.put("right", "present");
        return m;
    }

    @Override
    public Map<String, Difference> flattened() {
        return Map.of("ExtraField", this);
    }
}
