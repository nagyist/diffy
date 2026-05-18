package ai.diffy.analysis;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public record JoinedDifferences(RawDifferenceCounter raw, NoiseDifferenceCounter noise) {

    public Map<String, JoinedEndpoint> endpoints() {
        Map<String, JoinedEndpoint> result = new LinkedHashMap<>();
        for (String k : raw.counter().endpoints().keySet()) {
            result.put(k, endpoint(k));
        }
        return result;
    }

    public JoinedEndpoint endpoint(String endpointName) {
        EndpointMetadata ep         = raw.counter().endpoint(endpointName);
        Map<String, FieldMetadata> rawFields   = raw.counter().fields(endpointName);
        Map<String, FieldMetadata> noiseFields = noise.counter().fields(endpointName);
        return new JoinedEndpoint(ep, rawFields, noiseFields);
    }
}
