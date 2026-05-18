package ai.diffy.analysis;

public interface FieldMetadata {
    /** number of differences seen for this field */
    int differences();
    /** weight relative to other fields */
    int weight();

    FieldMetadata Empty = new FieldMetadata() {
        @Override public int differences() { return 0; }
        @Override public int weight() { return 0; }
    };
}
