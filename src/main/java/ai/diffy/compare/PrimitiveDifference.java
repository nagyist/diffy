package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;

public record PrimitiveDifference<A>(A left, A right) implements TerminalDifference {
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "PrimitiveDifference");
        m.put("left", left);
        m.put("right", right);
        return m;
    }
}
