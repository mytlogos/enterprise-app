package com.mytlogos.enterprise.background.api

import com.google.gson.*
import com.mytlogos.enterprise.formatDateTime
import com.mytlogos.enterprise.parseDateTime
import org.joda.time.DateTime
import java.lang.reflect.Type

class DateTimeAdapter : JsonDeserializer<DateTime?>, JsonSerializer<DateTime?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement?, typeOfT: Type,
                             context: JsonDeserializationContext): DateTime? {
        return json?.asString?.parseDateTime()
    }

    override fun serialize(src: DateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        return if (src == null) null else JsonPrimitive(src.formatDateTime())
    }
}