package ai.diffy.analysis;

public record Report(
    DifferenceAnalyzer differenceAnalyzer,
    JoinedDifferences joinedDifferences,
    InMemoryDifferenceCollector collector,
    long start,
    long end
) {}
