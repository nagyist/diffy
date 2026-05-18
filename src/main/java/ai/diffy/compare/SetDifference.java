package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record SetDifference<A>(Set<A> leftNotRight, Set<A> rightNotLeft) implements TerminalDifference {
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "SetDifference");
        m.put("left", leftNotRight);
        m.put("right", rightNotLeft);
        return m;
    }
}
