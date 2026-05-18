package ai.diffy.analysis;

public record JoinedField(EndpointMetadata endpoint, FieldMetadata raw, FieldMetadata noise) {

    /** percent difference out of total number of requests */
    public double absoluteDifference() {
        return Math.abs(raw.differences() - noise.differences()) / (double) endpoint.total() * 100;
    }

    /** square error between this field's differences and the noisy counterpart's differences */
    public double relativeDifference() {
        int sum = raw.differences() + noise.differences();
        if (sum == 0) return 0.0;
        return Math.abs(raw.differences() - noise.differences()) / (double) sum * 100;
    }
}
