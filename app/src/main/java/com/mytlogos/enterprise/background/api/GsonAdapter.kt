package com.mytlogos.enterprise.background.api

import com.google.gson.*
import com.mytlogos.enterprise.Formatter
import org.joda.time.DateTime
import java.lang.reflect.Type

internal class GsonAdapter {
    class DateTimeAdapter : JsonDeserializer<DateTime?>, JsonSerializer<DateTime?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement?, typeOfT: Type,
                                 context: JsonDeserializationContext): DateTime? {
            return if (json == null) null else Formatter.parseDateTime(json.asString)
        }

        override fun serialize(src: DateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
            return if (src == null) null else JsonPrimitive(Formatter.formatDateTime(src))
        }
    }

    class ArrayAdapter : JsonDeserializer<DateTime?>, JsonSerializer<DateTime?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement?, typeOfT: Type,
                                 context: JsonDeserializationContext): DateTime? {
            return if (json == null) null else Formatter.parseDateTime(json.asString)
        }

        override fun serialize(src: DateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
            return if (src == null) null else JsonPrimitive(Formatter.formatDateTime(src))
        }
    }
}