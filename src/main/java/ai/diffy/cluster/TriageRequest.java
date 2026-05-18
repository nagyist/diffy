package ai.diffy.cluster;

import java.util.List;

public class TriageRequest {
    public String endpoint;
    public String field;
    public String diffType;
    public long count;
    public List<RegressionCluster.SampleDiff> samples;
}
