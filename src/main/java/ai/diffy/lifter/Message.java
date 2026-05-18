package ai.diffy.lifter;

import java.util.Optional;

public record Message(Optional<String> endpoint, FieldMap result) {
    public Message(String endpoint, FieldMap result) {
        this(Optional.ofNullable(endpoint), result);
    }
}
