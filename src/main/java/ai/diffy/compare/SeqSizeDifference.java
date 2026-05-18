package ai.diffy.compare;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SeqSizeDifference<A>(List<A> leftNotRight, List<A> rightNotLeft)
        implements TerminalDifference, SeqDifference {
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "SeqSizeDifference");
        m.put("left", "missing elements: " + leftNotRight);
        m.put("right", "unexpected elements: " + rightNotLeft);
        return m;
    }
}
