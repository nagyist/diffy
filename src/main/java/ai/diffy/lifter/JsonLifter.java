package ai.diffy.lifter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class JsonLifter {

    public static final Logger log = LoggerFactory.getLogger(JsonLifter.class);

    // Singleton null marker
    @JsonSerialize(using = JsonNullSerializer.class)
    public enum JsonNull {
        INSTANCE;
    }

    public static class JsonParseError extends RuntimeException {
        public JsonParseError() { super("Json parse error"); }
    }

    public static final ObjectMapper Mapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(FieldMap.class, new FieldMapSerializer());
        Mapper.registerModule(module);
    }

    private static class FieldMapSerializer extends StdSerializer<FieldMap> {
        public FieldMapSerializer() { super(FieldMap.class); }
        @Override
        public void serialize(FieldMap t, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeObject(t.value);
        }
    }

    public static JsonNode apply(Object obj) {
        return Mapper.valueToTree(obj);
    }

    public static Object lift(JsonNode node) {
        JsonToken token = node.asToken();
        if (token == JsonToken.START_ARRAY) {
            List<Object> list = new ArrayList<>();
            node.elements().forEachRemaining(element -> list.add(lift(element)));
            return list;
        } else if (token == JsonToken.START_OBJECT) {
            Set<String> fields = new HashSet<>();
            node.fieldNames().forEachRemaining(fields::add);
            if (areMapInsteadofObjectKeys(fields)) {
                Map<String, Object> map = new LinkedHashMap<>();
                node.fields().forEachRemaining(entry -> map.put(entry.getKey(), lift(entry.getValue())));
                return map;
            } else {
                Map<String, Object> fmValue = new LinkedHashMap<>();
                node.fields().forEachRemaining(entry -> fmValue.put(entry.getKey(), lift(entry.getValue())));
                return new FieldMap(fmValue);
            }
        } else if (token == JsonToken.VALUE_FALSE) {
            return false;
        } else if (token == JsonToken.VALUE_NULL) {
            return JsonNull.INSTANCE;
        } else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            return node.asDouble();
        } else if (token == JsonToken.VALUE_NUMBER_INT) {
            return node.asLong();
        } else if (token == JsonToken.VALUE_TRUE) {
            return true;
        } else if (token == JsonToken.VALUE_STRING) {
            return node.textValue();
        } else {
            throw new JsonParseError();
        }
    }

    public static JsonNode decode(String json) {
        try {
            return Mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T decode(String json, Class<T> clss) {
        try {
            return Mapper.readValue(json, clss);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(Object item) {
        try {
            return Mapper.writer().writeValueAsString(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean areMapInsteadofObjectKeys(Set<String> fields) {
        if (fields.size() > 50) return true;
        for (String field : fields) {
            if (field.length() > 100) return true;
            if (field.matches("[0-9].*")) return true;  // starts with digit
            if (!field.matches("[_a-zA-Z0-9]*")) return true; // non-alphanumeric
        }
        return false;
    }
}
