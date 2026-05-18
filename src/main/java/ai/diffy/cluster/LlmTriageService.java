package ai.diffy.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LlmTriageService {

    private static final Logger log = LoggerFactory.getLogger(LlmTriageService.class);

    // "ollama" (default) or "anthropic"
    @Value("${llm.provider:ollama}")
    private String provider;

    @Value("${llm.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${llm.ollama.model:phi3:mini}")
    private String ollamaModel;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${anthropic.model:claude-opus-4-7}")
    private String anthropicModel;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TriageResult triage(TriageRequest request) {
        String prompt = buildPrompt(request);
        try {
            if ("anthropic".equals(provider)) {
                return triageWithAnthropic(prompt);
            } else {
                return triageWithOllama(prompt);
            }
        } catch (Exception e) {
            log.error("LLM triage failed (provider={})", provider, e);
            return TriageResult.error("Triage failed: " + e.getMessage());
        }
    }

    // ── Ollama / llama-server (default) ──────────────────────────────────────

    private TriageResult triageWithOllama(String prompt) throws Exception {
        String url = ollamaUrl.stripTrailing() + "/v1/chat/completions";

        String body = objectMapper.writeValueAsString(Map.of(
                "model", ollamaModel,
                "stream", false,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            log.warn("Ollama returned {}: {}", res.statusCode(), res.body());
            if (res.body() == null || res.body().isBlank()) {
                return TriageResult.error(
                        "LLM server not reachable at " + ollamaUrl +
                        ". For local dev run: ollama serve && ollama pull " + ollamaModel);
            }
            return TriageResult.error("LLM server error " + res.statusCode() + ": " + res.body());
        }

        JsonNode root = objectMapper.readTree(res.body());
        String text = root.path("choices").get(0).path("message").path("content").asText();
        TriageResult result = parseJsonPayload(text);
        result.provider = "ollama/" + ollamaModel;
        return result;
    }

    // ── Anthropic fallback ────────────────────────────────────────────────────

    private TriageResult triageWithAnthropic(String prompt) throws Exception {
        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            return TriageResult.error("anthropic.api-key is not configured");
        }

        String body = objectMapper.writeValueAsString(Map.of(
                "model", anthropicModel,
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            log.warn("Anthropic API returned {}: {}", res.statusCode(), res.body());
            return TriageResult.error("Anthropic API error: " + res.statusCode());
        }

        JsonNode root = objectMapper.readTree(res.body());
        String text = root.path("content").get(0).path("text").asText();
        TriageResult result = parseJsonPayload(text);
        result.provider = "anthropic/" + anthropicModel;
        return result;
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private String buildPrompt(TriageRequest req) {
        String samplesText = req.samples == null ? "  none" :
                req.samples.stream().map(s ->
                        "  - left (primary): " + s.left + "\n    right (candidate): " + s.right
                ).collect(Collectors.joining("\n"));

        return String.format(
            "You are analyzing API regression data from Diffy, a regression testing proxy.\n\n" +
            "A cluster of similar field differences has been detected:\n" +
            "- Endpoint: %s\n" +
            "- Field path: %s\n" +
            "- Difference type: %s\n" +
            "- Occurrence count: %d\n" +
            "- Sample values:\n%s\n\n" +
            "Respond with a JSON object containing exactly these fields:\n" +
            "{\n" +
            "  \"summary\": \"<one sentence plain English description of what changed>\",\n" +
            "  \"severity\": \"<one of: regression, intentional, unknown>\",\n" +
            "  \"recommendation\": \"<one of: mark_noise, investigate, ignore>\"\n" +
            "}\n\n" +
            "Guidelines:\n" +
            "- regression: the change is likely a bug (wrong value, missing field, type error)\n" +
            "- intentional: the change is likely a deliberate improvement\n" +
            "- unknown: cannot determine without more context\n" +
            "- mark_noise: field is non-deterministic (timestamps, IDs, random values)\n" +
            "- investigate: needs human review\n" +
            "- ignore: difference is cosmetic and harmless\n\n" +
            "Respond with only the JSON object, no other text.\n",
            req.endpoint, req.field, req.diffType, req.count, samplesText
        );
    }

    private TriageResult parseJsonPayload(String text) throws Exception {
        text = text.strip();
        if (text.startsWith("```")) {
            text = text.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
        }
        JsonNode parsed = objectMapper.readTree(text);
        TriageResult result = new TriageResult();
        result.summary = parsed.path("summary").asText();
        result.severity = parsed.path("severity").asText("unknown");
        result.recommendation = parsed.path("recommendation").asText("investigate");
        return result;
    }
}
