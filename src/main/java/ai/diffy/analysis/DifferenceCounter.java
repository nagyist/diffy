package ai.diffy.analysis;

import ai.diffy.compare.Difference;

import java.util.Map;

public interface DifferenceCounter {
    void count(String endpoint, Map<String, Difference> diffs);
    Map<String, EndpointMetadata> endpoints();
    default EndpointMetadata endpoint(String endpoint) { return endpoints().get(endpoint); }
    Map<String, FieldMetadata> fields(String endpoint);
    void clear();
}
