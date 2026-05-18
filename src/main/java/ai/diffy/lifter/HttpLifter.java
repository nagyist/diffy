package ai.diffy.lifter;

import ai.diffy.Settings;
import ai.diffy.proxy.HttpMessage;
import ai.diffy.proxy.HttpRequest;
import ai.diffy.proxy.HttpResponse;
import ai.diffy.util.ResourceMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class HttpLifter {

    public static final String ControllerEndpointHeaderName = "X-Action-Name";

    public static Exception contentTypeNotSupportedException(String contentType) {
        return new Exception("Content type: " + contentType + " is not supported");
    }

    public static class MalformedJsonContentException extends Exception {
        public MalformedJsonContentException(Throwable cause) {
            super("Malformed Json content");
            initCause(cause);
        }
    }

    private final boolean excludeHttpHeadersComparison;
    private final Optional<ResourceMatcher> resourceMatcher;

    @Autowired
    public HttpLifter(Settings settings) {
        this.excludeHttpHeadersComparison = settings.excludeHttpHeadersComparison();
        this.resourceMatcher = settings.resourceMatcher();
    }

    private Map<String, Object> headersMap(HttpMessage response) {
        if (!excludeHttpHeadersComparison) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            response.getHeaders().forEach((k, v) -> normalized.put(k.toLowerCase(), v));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("headers", new FieldMap(normalized));
            return m;
        }
        return Map.of();
    }

    public Message liftRequest(HttpRequest req) {
        Map<String, String> headers = req.getHeaders();

        Optional<String> canonicalResource = Optional.ofNullable(headers.get("Canonical-Resource"))
            .or(() -> resourceMatcher.flatMap(rm -> rm.resourceName(req.getPath())))
            .or(() -> Optional.of(req.getMethod() + ":" + req.getPath()));

        Object body = StringLifter.lift(req.getBody());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("method",  req.getMethod());
        m.put("path",    req.getPath());
        m.put("uri",     req.getUri());
        m.put("headers", headers);
        m.put("params",  req.getParams());
        m.put("body",    body);

        return new Message(canonicalResource, new FieldMap(m));
    }

    public Message liftResponse(HttpResponse r) {
        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("status", r.getStatus());
        responseMap.put("body",   StringLifter.lift(r.getBody()));
        responseMap.putAll(headersMap(r));
        return new Message(Optional.empty(), new FieldMap(responseMap));
    }
}
