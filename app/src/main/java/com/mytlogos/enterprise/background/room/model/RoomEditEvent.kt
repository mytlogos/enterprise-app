package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import com.mytlogos.enterprise.background.EditEvent
import org.joda.time.DateTime

@Entity(primaryKeys = ["id", "objectType", "eventType", "dateTime"])
data class RoomEditEvent(
    override val id: Int,
    override val objectType: Int,
    override val eventType: Int,
    override val dateTime: DateTime,
    override val firstValue: String,
    override val secondValue: String,
) : EditEvent