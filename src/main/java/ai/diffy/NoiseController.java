package ai.diffy;

import ai.diffy.repository.Noise;
import ai.diffy.repository.NoiseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class NoiseController {

    public record Mark(boolean isNoise) {}

    private final NoiseRepository noise;

    @Autowired
    public NoiseController(NoiseRepository noise) {
        this.noise = noise;
    }

    private java.util.List<String> empty() {
        return new ArrayList<>();
    }

    @GetMapping(path = "/api/1/noise")
    public Map<String, java.util.List<String>> getAllNoise() {
        return noise.findAll().stream()
            .collect(Collectors.toMap(
                n -> n.endpoint,
                n -> n.noisyfields
            ));
    }

    @GetMapping(path = "/api/1/noise/{endpoint}")
    public java.util.List<String> getNoise(@PathVariable("endpoint") String endpoint) {
        return noise.findById(endpoint).map(n -> n.noisyfields).orElse(empty());
    }

    @PostMapping(path = "/api/1/noise/{endpoint}/prefix/{fieldPrefix}")
    public boolean postNoise(
            @PathVariable("endpoint") String endpoint,
            @PathVariable("fieldPrefix") String fieldPrefix,
            @RequestBody Mark mark) {
        java.util.List<String> noisyFields =
            noise.findById(endpoint).map(n -> n.noisyfields).orElse(empty());
        boolean success;
        if (mark.isNoise()) {
            success = noisyFields.add(fieldPrefix);
        } else {
            success = noisyFields.remove(fieldPrefix);
        }
        noise.save(new Noise(endpoint, noisyFields));
        return success;
    }
}
