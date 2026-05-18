package ai.diffy;

import ai.diffy.Settings.HostPort;
import ai.diffy.proxy.ReactorHttpDifferenceProxy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sends traffic through the proxy with a candidate that uppercases responses,
 * then verifies the expected field-level diffs appear in the overview API.
 *
 * Backends are plain Reactor Netty servers (no GraalVM JS) so the test
 * is compatible with any JDK version.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OverviewDiffTest {

    @Autowired Settings settings;
    @Autowired ReactorHttpDifferenceProxy proxy;

    private DisposableServer primary, secondary, candidate;
    private final RestTemplate restTemplate = new RestTemplate();

    /** Creates a minimal HTTP server that transforms the request body before echoing it back. */
    private static DisposableServer startEchoServer(int port, UnaryOperator<String> bodyTransform) {
        return HttpServer.create()
                .port(port)
                .handle((req, res) ->
                        req.receive().aggregate().asString()
                                .defaultIfEmpty("")
                                .flatMap(body ->
                                        res.status(HttpResponseStatus.OK)
                                                .sendString(Mono.just(bodyTransform.apply(body)))
                                                .then()))
                .bindNow();
    }

    /** Uppercases a plain-text body or — for a JSON body — uppercases both keys and string values. */
    private static String uppercase(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            // Simple single-level JSON object: {"key":"value",...}
            String inner = trimmed.substring(1, trimmed.length() - 1);
            StringBuilder sb = new StringBuilder("{");
            for (String pair : inner.split(",")) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    if (sb.length() > 1) sb.append(",");
                    sb.append(kv[0].toUpperCase()).append(":").append(kv[1].toUpperCase());
                }
            }
            sb.append("}");
            return sb.toString();
        }
        return body.toUpperCase();
    }

    @BeforeAll
    public void setup() {
        int primaryPort   = ((HostPort) settings.primary()).port();
        int secondaryPort = ((HostPort) settings.secondary()).port();
        int candidatePort = ((HostPort) settings.candidate()).port();
        primary   = startEchoServer(primaryPort,   body -> body);
        secondary = startEchoServer(secondaryPort, body -> body);
        candidate = startEchoServer(candidatePort, OverviewDiffTest::uppercase);
    }

    @AfterAll
    public void shutdown() {
        primary.disposeNow();
        secondary.disposeNow();
        candidate.disposeNow();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void diffsShouldAppearInOverview() throws InterruptedException {
        String proxyBase = "http://localhost:" + settings.servicePort();
        String apiBase   = "http://localhost:8888";

        HttpHeaders textHeaders = new HttpHeaders();
        textHeaders.set("Content-Type", "application/text;Canonical-Resource:text");

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.set("Content-Type", "application/json;Canonical-Resource:json");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            restTemplate.postForObject(proxyBase + "/text",
                    new HttpEntity<>("Twitter", textHeaders), String.class);
            restTemplate.postForObject(proxyBase + "/json",
                    new HttpEntity<>("{\"name\":\"Microsoft\"}", jsonHeaders), String.class);
        }

        long end = System.currentTimeMillis();

        // Allow async persistence to complete before querying
        Thread.sleep(500);

        Map<String, Object> overview = restTemplate.getForObject(
                apiBase + "/api/1/overview?start=" + start + "&end=" + end,
                Map.class);

        assertNotNull(overview);

        // ── POST:text ──────────────────────────────────────────────────────────
        assertTrue(overview.containsKey("POST:text"),
                "Expected POST:text endpoint in overview, got: " + overview.keySet());

        Map<String, Object> textEndpoint = (Map<String, Object>) overview.get("POST:text");
        Map<String, Object> textFields   = (Map<String, Object>) textEndpoint.get("fields");

        assertTrue(textFields.containsKey("response.body.PrimitiveDifference"),
                "Expected response.body.PrimitiveDifference in POST:text fields, got: " + textFields.keySet());

        int textDiffs = ((Number) ((Map<String, Object>) textFields
                .get("response.body.PrimitiveDifference")).get("differences")).intValue();
        assertTrue(textDiffs > 0,
                "Expected differences > 0 for response.body.PrimitiveDifference, got " + textDiffs);

        // ── POST:json ──────────────────────────────────────────────────────────
        assertTrue(overview.containsKey("POST:json"),
                "Expected POST:json endpoint in overview, got: " + overview.keySet());

        Map<String, Object> jsonEndpoint = (Map<String, Object>) overview.get("POST:json");
        Map<String, Object> jsonFields   = (Map<String, Object>) jsonEndpoint.get("fields");

        assertTrue(jsonFields.containsKey("response.body.value.name.MissingField"),
                "Expected response.body.value.name.MissingField in POST:json fields, got: " + jsonFields.keySet());
        assertTrue(jsonFields.containsKey("response.body.value.NAME.ExtraField"),
                "Expected response.body.value.NAME.ExtraField in POST:json fields, got: " + jsonFields.keySet());

        int missingDiffs = ((Number) ((Map<String, Object>) jsonFields
                .get("response.body.value.name.MissingField")).get("differences")).intValue();
        int extraDiffs   = ((Number) ((Map<String, Object>) jsonFields
                .get("response.body.value.NAME.ExtraField")).get("differences")).intValue();

        assertTrue(missingDiffs > 0,
                "Expected differences > 0 for response.body.value.name.MissingField, got " + missingDiffs);
        assertTrue(extraDiffs > 0,
                "Expected differences > 0 for response.body.value.NAME.ExtraField, got " + extraDiffs);
    }
}
