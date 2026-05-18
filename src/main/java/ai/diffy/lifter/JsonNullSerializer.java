package ai.diffy.lifter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class JsonNullSerializer extends StdSerializer<Object> {

    public JsonNullSerializer() {
        super(Object.class);
    }

    @Override
    public void serialize(Object t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNull();
    }
}
