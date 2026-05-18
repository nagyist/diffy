package ai.diffy.analysis;

public interface EndpointMetadata {
    /** number of differences seen at this endpoint */
    int differences();
    /** total number of requests seen for this endpoint */
    int total();
}
