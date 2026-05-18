package ai.diffy.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceMatcher {

    public record ResourceMapping(String pattern, String name) {}

    private static final String TOKEN_PATTERN  = ":\\w+";
    private static final String WILDCARD_PATTERN = "\\*+";

    // List of (compiledRegex, resourceName) pairs
    private final List<ResourceMapping> patterns;

    public ResourceMatcher(List<ResourceMapping> mappings) {
        this.patterns = mappings.stream()
            .map(m -> new ResourceMapping(
                m.pattern()
                    .replaceAll(TOKEN_PATTERN, "\\\\w+")
                    .replaceAll(WILDCARD_PATTERN, ".*"),
                m.name()
            ))
            .collect(Collectors.toList());
    }

    public Optional<String> resourceName(String path) {
        return patterns.stream()
            .filter(m -> path.matches(m.pattern()))
            .map(ResourceMapping::name)
            .findFirst();
    }
}
