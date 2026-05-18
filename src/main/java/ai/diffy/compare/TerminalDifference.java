package ai.diffy.compare;

import java.util.Map;

/**
 * Base marker for terminal (leaf) difference nodes.
 * flattened() returns Map of { SimpleClassName -> this }.
 */
public interface TerminalDifference extends Difference {
    @Override
    default Map<String, Difference> flattened() {
        return Map.of(getClass().getSimpleName(), this);
    }
}
