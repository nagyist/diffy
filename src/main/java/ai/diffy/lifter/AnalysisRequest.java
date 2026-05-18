package ai.diffy.lifter;

public record AnalysisRequest(
    Message request,
    Message candidate,
    Message primary,
    Message secondary
) {}
