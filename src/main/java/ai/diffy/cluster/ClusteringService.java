package ai.diffy.cluster;

import ai.diffy.analysis.DifferenceResult;
import ai.diffy.analysis.FieldDifference;
import ai.diffy.repository.DifferenceResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClusteringService {

    @Autowired
    private DifferenceResultRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<RegressionCluster> getClusters(long start, long end) {
        List<DifferenceResult> results = repository.findByTimestampMsecBetween(start, end);
        Map<String, RegressionCluster> clusters = new LinkedHashMap<>();

        for (DifferenceResult dr : results) {
            if (dr.differences == null) continue;
            for (FieldDifference fd : dr.differences) {
                String diffType = parseDiffType(fd.difference);
                // Skip NoDifference entries — they are request fields, not regressions
                if ("NoDifference".equals(diffType)) continue;

                String key = dr.endpoint + "|" + fd.field + "|" + diffType;
                RegressionCluster cluster = clusters.computeIfAbsent(key, k -> {
                    RegressionCluster c = new RegressionCluster();
                    c.id = Integer.toHexString(key.hashCode());
                    c.endpoint = dr.endpoint;
                    c.field = fd.field;
                    c.diffType = diffType;
                    c.count = 0;
                    return c;
                });

                cluster.count++;
                if (cluster.samples.size() < 3) {
                    cluster.samples.add(parseSample(dr.id, fd.difference));
                }
            }
        }

        return clusters.values().stream()
                .sorted((a, b) -> Long.compare(b.count, a.count))
                .collect(Collectors.toList());
    }

    private String parseDiffType(String differenceJson) {
        try {
            JsonNode node = objectMapper.readTree(differenceJson);
            JsonNode typeNode = node.get("type");
            return typeNode != null ? typeNode.asText() : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private RegressionCluster.SampleDiff parseSample(String requestId, String differenceJson) {
        try {
            JsonNode node = objectMapper.readTree(differenceJson);
            Object left = node.has("left") ? node.get("left") : null;
            Object right = node.has("right") ? node.get("right") : null;
            return new RegressionCluster.SampleDiff(requestId, left, right);
        } catch (Exception e) {
            return new RegressionCluster.SampleDiff(requestId, differenceJson, null);
        }
    }
}
