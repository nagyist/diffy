package ai.diffy;

import ai.diffy.Settings.Downstream;
import ai.diffy.Settings.HostPort;
import ai.diffy.proxy.ReactorHttpDifferenceProxy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);
    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    Settings settings;

    @Autowired
    ReactorHttpDifferenceProxy proxy;

    private int extractPort(Downstream downstream) {
        return ((HostPort) downstream).port();
    }

    DisposableServer primary, secondary, candidate;
    String proxyUrl;

    @BeforeAll
    public void setup() {
        primary   = startEchoServer(extractPort(settings.primary()));
        secondary = startEchoServer(extractPort(settings.secondary()));
        candidate = startEchoServer(extractPort(settings.candidate()));
        proxyUrl = "http://localhost:" + settings.servicePort() + "/base";
    }

    private static DisposableServer startEchoServer(int port) {
        return HttpServer.create()
                .port(port)
                .httpRequestDecoder(spec -> spec.maxHeaderSize(64 * 1024 * 1024))
                .handle((req, res) ->
                        req.receive().aggregate().asString()
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    req.requestHeaders().forEach(entry -> {
                                        String name = entry.getKey();
                                        if (!name.equalsIgnoreCase("Content-Length")) {
                                            res.addHeader(name, entry.getValue());
                                        }
                                    });
                                    return res.status(HttpResponseStatus.OK)
                                            .sendString(Mono.just(body))
                                            .then();
                                }))
                .bindNow();
    }

    @AfterAll
    public void shutdown() {
        primary.disposeNow();
        secondary.disposeNow();
        candidate.disposeNow();
    }

    @Test
    public void warmup() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        FileSystemResource payload = new FileSystemResource("src/test/resources/payload.json");
        String json = FileCopyUtils.copyToString(new InputStreamReader(payload.getInputStream()));
        String response = restTemplate.postForObject(proxyUrl, new HttpEntity<>(json, headers), String.class);
        assertEquals(json, response);
    }

    @Test
    public void largeRequestBody() {
        int largeSize = 16 * 1024 * 1024; // 16 MB
        String json = "{\"a\":\"" + new String(new char[largeSize]) + "\"}";
        log.info("Testing request body of {} MB", json.getBytes().length / 1024 / 1024);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String response = restTemplate.postForObject(proxyUrl, new HttpEntity<>(json, headers), String.class);
        assertEquals(json, response);
    }

    @Test
    public void largeRequestHeaders() {
        int largeSize = 16 * 1024 * 1024; // 16 MB
        String json = "{\"a\":\"\"}";
        String header = new String(new char[largeSize]).replaceAll(".", "0");
        log.info("Testing request header of {} MB", header.getBytes().length / 1024 / 1024);

        // JDK 24+ HttpURLConnection limits request headers to 384 KB; use Reactor Netty client instead
        String[] responseHeader = new String[1];
        String responseBody = HttpClient.create()
                .httpResponseDecoder(spec -> spec.maxHeaderSize(64 * 1024 * 1024))
                .headers(h -> {
                    h.set("Content-Type", "application/json");
                    h.set("a", header);
                })
                .post()
                .uri(proxyUrl)
                .send(reactor.netty.ByteBufMono.fromString(Mono.just(json)))
                .responseSingle((resp, body) -> {
                    responseHeader[0] = resp.responseHeaders().get("a");
                    return body.asString();
                })
                .block(Duration.ofSeconds(30));

        assertEquals(json, responseBody);
        assertEquals(header, responseHeader[0]);
    }
}
