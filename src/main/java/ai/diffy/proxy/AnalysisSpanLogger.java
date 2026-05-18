package ai.diffy.proxy;

import ai.diffy.analysis.DifferenceResult;
import ai.diffy.lifter.AnalysisRequest;
import ai.diffy.functional.topology.TriConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import java.util.Optional;

public class AnalysisSpanLogger implements TriConsumer<AnalysisRequest, Optional<DifferenceResult>, Span> {
    private AnalysisSpanLogger(){};
    public static AnalysisSpanLogger INSTANCE = new AnalysisSpanLogger();
    @Override
    public void accept(AnalysisRequest analysisRequest, Optional<DifferenceResult> result, Span span) {
        result.ifPresent(diffResult ->
                span.addEvent("DifferenceResult", Attributes.of(
                        AttributeKey.stringKey("endpoint"), diffResult.endpoint,
                        AttributeKey.stringKey("request"), diffResult.request,
                        AttributeKey.stringKey("candidate"), diffResult.responses.candidate,
                        AttributeKey.stringKey("primary"), diffResult.responses.primary,
                        AttributeKey.stringKey("secondary"), diffResult.responses.secondary
                ))
        );
    }
}
