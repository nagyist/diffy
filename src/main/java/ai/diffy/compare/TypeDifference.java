package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.Map;

public record TypeDifference<A, B>(A left, B right) implements TerminalDifference {
    private static String toMessage(Object obj) {
        return obj.getClass().getSimpleName() + ": " + obj;
    }
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "TypeDifference");
        m.put("left", toMessage(left));
        m.put("right", toMessage(right));
        return m;
    }
}
