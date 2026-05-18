package ai.diffy.compare;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;

public class DifferenceTest {
    private static final Logger log = LoggerFactory.getLogger(DifferenceTest.class);

    @Test
    public void payloadTest() throws IOException {
        FileSystemResource payload = new FileSystemResource("src/test/resources/payload.json");
        String json = FileCopyUtils.copyToString(new InputStreamReader(payload.getInputStream()));
        log.info(json);
        Difference diff = Difference.apply(json, json);
        diff.flattened().forEach((key, value) -> {
            log.info("{} - {}", key, value);
        });
    }
}
