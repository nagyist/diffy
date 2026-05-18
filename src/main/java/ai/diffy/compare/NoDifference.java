package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;

public record NoDifference<A>(A value) implements TerminalDifference {
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "NoDifference");
        m.put("left", value);
        m.put("right", value);
        return m;
    }
}
