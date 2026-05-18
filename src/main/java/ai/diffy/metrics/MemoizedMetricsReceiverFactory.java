package ai.diffy.metrics;

import ai.diffy.util.Memoize;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Package-private implementation of MetricsReceiver, and its factory.
 */
class MemoizedMetricsReceiver implements MetricsReceiver {
    private final String name;
    private final Map<String, String> tags;
    private volatile Counter _counter;

    MemoizedMetricsReceiver(String name, Map<String, String> tags) {
        this.name = name;
        this.tags = tags;
    }

    @Override
    public Counter counter() {
        if (_counter == null) {
            synchronized (this) {
                if (_counter == null) {
                    Tags micrTags = Tags.of(tags.entrySet().stream()
                        .map(e -> Tag.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList()));
                    _counter = Metrics.globalRegistry.counter(name, micrTags);
                }
            }
        }
        return _counter;
    }

    @Override
    public MetricsReceiver withNameToken(String token) {
        return new MemoizedMetricsReceiver(name + "_" + token, tags);
    }

    @Override
    public MetricsReceiver withAdditionalTags(Map<String, String> additionalTags) {
        Map<String, String> merged = new HashMap<>(tags);
        merged.putAll(additionalTags);
        return new MemoizedMetricsReceiver(name, merged);
    }
}

/**
 * Holds the single root MetricsReceiver instance.
 */
public class MemoizedMetricsReceiverFactory {
    public static final MetricsReceiver ROOT = new MemoizedMetricsReceiver("diffy", new HashMap<>());
}
