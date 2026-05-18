package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record OrderingDifference(List<Integer> leftPattern, List<Integer> rightPattern)
        implements TerminalDifference, SeqDifference {
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "OrderingDifference");
        m.put("left", leftPattern);
        m.put("right", rightPattern);
        return m;
    }
}
