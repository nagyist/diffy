package ai.diffy.cluster;

public class TriageResult {
    public String summary;
    // "regression", "intentional", "unknown"
    public String severity;
    // "mark_noise", "investigate", "ignore"
    public String recommendation;
    // e.g. "ollama/phi3:mini" or "anthropic/claude-opus-4-7"
    public String provider;
    public String error;

    public static TriageResult error(String message) {
        TriageResult r = new TriageResult();
        r.error = message;
        r.severity = "unknown";
        r.recommendation = "investigate";
        return r;
    }
}
