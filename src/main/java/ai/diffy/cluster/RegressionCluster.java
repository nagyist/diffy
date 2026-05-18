package ai.diffy.cluster;

import java.util.ArrayList;
import java.util.List;

public class RegressionCluster {
    public String id;
    public String endpoint;
    public String field;
    public String diffType;
    public long count;
    public List<SampleDiff> samples = new ArrayList<>();

    public static class SampleDiff {
        public String requestId;
        public Object left;
        public Object right;

        public SampleDiff() {}

        public SampleDiff(String requestId, Object left, Object right) {
            this.requestId = requestId;
            this.left = left;
            this.right = right;
        }
    }
}
