package ai.diffy.metrics;

import io.micrometer.core.instrument.Counter;

import java.util.Map;

public interface MetricsReceiver {
    MetricsReceiver withNameToken(String name);
    MetricsReceiver withAdditionalTags(Map<String, String> tags);
    Counter counter();

    static MetricsReceiver root() {
        return MemoizedMetricsReceiverFactory.ROOT;
    }
}
