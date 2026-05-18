package ai.diffy;

import ai.diffy.functional.functions.Try;
import ai.diffy.util.ResourceMatcher;
import ai.diffy.util.ResponseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Settings {

    private static final Logger log = LoggerFactory.getLogger(Settings.class);

    public final int servicePort;
    public final Downstream candidate;
    public final Downstream primary;
    public final Downstream secondary;
    public final String protocol;
    public final String serviceName;
    public final String apiRoot;
    public final double relativeThreshold;
    public final double absoluteThreshold;
    public final String rootUrl;
    public final boolean allowHttpSideEffects;
    public final boolean excludeHttpHeadersComparison;
    public final int maxHeaderSize;
    public final Optional<ResourceMatcher> resourceMatcher;
    public final ResponseMode responseMode;
    public final boolean dockerComposeLocal;

    public Settings(
            @Value("${proxy.port}") int servicePort,
            @Value("${candidate}") String candidateAddress,
            @Value("${master.primary}") String primaryAddress,
            @Value("${master.secondary}") String secondaryAddress,
            @Value("${service.protocol}") String protocol,
            @Value("${serviceName}") String serviceName,
            @Value("${apiRoot:}") String apiRoot,
            @Value("${threshold.relative:20.0}") double relativeThreshold,
            @Value("${threshold.absolute:0.03}") double absoluteThreshold,
            @Value("${rootUrl:}") String rootUrl,
            @Value("${allowHttpSideEffects:false}") boolean allowHttpSideEffects,
            @Value("${excludeHttpHeadersComparison:false}") boolean excludeHttpHeadersComparison,
            @Value("${maxHeaderSize:8192}") int maxHeaderSize,
            @Value("${resource.mapping:}") String resourceMappings,
            @Value("${responseMode:primary}") String mode,
            @Value("${dockerComposeLocal:false}") boolean dockerComposeLocal) {

        this.servicePort               = servicePort;
        this.protocol                  = protocol;
        this.serviceName               = serviceName;
        this.apiRoot                   = apiRoot;
        this.relativeThreshold         = relativeThreshold;
        this.absoluteThreshold         = absoluteThreshold;
        this.rootUrl                   = rootUrl;
        this.allowHttpSideEffects      = allowHttpSideEffects;
        this.excludeHttpHeadersComparison = excludeHttpHeadersComparison;
        this.maxHeaderSize             = maxHeaderSize;
        this.responseMode              = ResponseMode.valueOf(mode);
        this.dockerComposeLocal        = dockerComposeLocal;

        this.candidate  = Downstream.of(candidateAddress);
        this.primary    = Downstream.of(primaryAddress);
        this.secondary  = Downstream.of(secondaryAddress);

        // Parse resource mappings: "pattern;name,pattern;name,..."
        String rm = (resourceMappings == null) ? "" : resourceMappings;
        String[] rmParts = rm.split(",");
        List<ResourceMatcher.ResourceMapping> mappings = Arrays.stream(rmParts)
            .map(s -> s.split(";"))
            .filter(parts -> {
                boolean ok = parts.length == 2;
                if (!ok && parts.length > 0 && !parts[0].isBlank()) {
                    log.warn("Malformed resource mapping: {}. Should be <pattern>;<resource-name>",
                        Arrays.toString(parts));
                }
                return ok;
            })
            .map(parts -> new ResourceMatcher.ResourceMapping(parts[0], parts[1]))
            .collect(Collectors.toList());

        this.resourceMatcher = mappings.isEmpty() ? Optional.empty()
            : Optional.of(new ResourceMatcher(mappings));
    }

    // =========================================================================
    // Downstream sealed hierarchy
    // =========================================================================

    public sealed interface Downstream permits HostPort, BaseUrl {
        static Downstream of(String address) {
            if (Try.of(() -> new URL(address)).isNormal()) {
                return new BaseUrl(address);
            }
            String[] parts = address.split(":");
            return new HostPort(parts[0], Integer.parseInt(parts[1]));
        }
    }

    public record HostPort(String host, int port) implements Downstream {
        @Override
        public String toString() { return host + ":" + port; }
    }

    public record BaseUrl(String baseUrl) implements Downstream {
        @Override
        public String toString() { return baseUrl; }
    }

    // =========================================================================
    // Accessors (for compatibility with code that uses getter-style)
    // =========================================================================
    public int servicePort()                          { return servicePort; }
    public Downstream primary()                       { return primary; }
    public Downstream secondary()                     { return secondary; }
    public Downstream candidate()                     { return candidate; }
    public int maxHeaderSize()                        { return maxHeaderSize; }
    public boolean allowHttpSideEffects()             { return allowHttpSideEffects; }
    public boolean excludeHttpHeadersComparison()     { return excludeHttpHeadersComparison; }
    public ResponseMode responseMode()                { return responseMode; }
    public Optional<ResourceMatcher> resourceMatcher() { return resourceMatcher; }
}
