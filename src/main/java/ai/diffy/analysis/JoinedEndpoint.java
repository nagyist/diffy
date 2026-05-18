package ai.diffy.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

public record JoinedEndpoint(
    EndpointMetadata endpoint,
    Map<String, FieldMetadata> original,
    Map<String, FieldMetadata> noise
) {
    public int differences() { return endpoint.differences(); }
    public int total()       { return endpoint.total(); }

    public Map<String, JoinedField> fields() {
        Map<String, JoinedField> result = new LinkedHashMap<>();
        original.forEach((path, field) ->
            result.put(path, new JoinedField(
                endpoint,
                field,
                noise.getOrDefault(path, FieldMetadata.Empty)
            ))
        );
        return result;
    }
}
