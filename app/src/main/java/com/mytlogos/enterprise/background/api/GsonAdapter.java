package com.mytlogos.enterprise.background.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mytlogos.enterprise.Formatter;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class GsonAdapter {
    public static class DateTimeAdapter implements JsonDeserializer<DateTime>,
            JsonSerializer<DateTime> {

        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
            return json == null ? null : Formatter.parseDateTime(json.getAsString());
        }

        @Override
        public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? null : new JsonPrimitive(Formatter.formatDateTime(src));
        }
    }
}
