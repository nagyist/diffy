package ai.diffy.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClusterController {

    @Autowired
    private ClusteringService clusteringService;

    @Autowired
    private LlmTriageService llmTriageService;

    @GetMapping("/api/1/clusters")
    public List<RegressionCluster> getClusters(
            @RequestParam(name = "start", defaultValue = "0") long start,
            @RequestParam(name = "end", defaultValue = "1701001001000") long end) {
        return clusteringService.getClusters(start, end);
    }

    @PostMapping("/api/1/clusters/triage")
    public TriageResult triage(@RequestBody TriageRequest request) {
        return llmTriageService.triage(request);
    }
}
