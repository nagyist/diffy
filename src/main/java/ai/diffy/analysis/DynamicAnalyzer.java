package ai.diffy.analysis;

import ai.diffy.lifter.FieldMap;
import ai.diffy.lifter.JsonLifter;
import ai.diffy.lifter.Message;
import ai.diffy.repository.DifferenceResultRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Filters a DifferenceAnalyzer using a specified time range to output another DifferenceAnalyzer.
 */
public class DynamicAnalyzer {

    private final DifferenceResultRepository repository;

    public DynamicAnalyzer(DifferenceResultRepository repository) {
        this.repository = repository;
    }

    public static FieldMap decodeFieldMap(String payload) {
        return objectNodeToFieldMap((ObjectNode) JsonLifter.decode(payload));
    }

    public static FieldMap objectNodeToFieldMap(ObjectNode objectNode) {
        Map<String, Object> acc = new LinkedHashMap<>();
        objectNode.fields().forEachRemaining(entry ->
            acc.put(entry.getKey(), entry.getValue())
        );
        if (acc.containsKey("headers")) {
            acc.put("headers", objectNodeToFieldMap((ObjectNode) acc.get("headers")));
        }
        return new FieldMap(acc);
    }

    public Report filter(long start, long end) {
        InMemoryDifferenceCollector collector = new InMemoryDifferenceCollector();
        RawDifferenceCounter raw   = new RawDifferenceCounter(InMemoryDifferenceCollector.newCounter("raw"));
        NoiseDifferenceCounter noise = new NoiseDifferenceCounter(InMemoryDifferenceCollector.newCounter("noise"));
        JoinedDifferences joinedDifferences = new JoinedDifferences(raw, noise);
        DifferenceAnalyzer analyzer = new DifferenceAnalyzer(raw, noise, collector);

        repository.findByTimestampMsecBetween(start, end).forEach(dr -> {
            Message request   = new Message(Optional.of(dr.endpoint), decodeFieldMap(dr.request));
            Message primary   = new Message(Optional.of(dr.endpoint), decodeFieldMap(dr.responses.primary));
            Message secondary = new Message(Optional.of(dr.endpoint), decodeFieldMap(dr.responses.secondary));
            Message candidate = new Message(Optional.of(dr.endpoint), decodeFieldMap(dr.responses.candidate));
            analyzer.apply(request, candidate, primary, secondary, Optional.of(dr.id));
        });

        return new Report(analyzer, joinedDifferences, collector, start, end);
    }
}
