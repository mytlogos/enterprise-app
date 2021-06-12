package com.mytlogos.enterprise.background.room.model

import androidx.room.Relation
import com.mytlogos.enterprise.model.Indexable
import org.joda.time.DateTime

class RoomTocEpisode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    override val partialIndex: Int,
    override val totalIndex: Int,
    val readDate: DateTime?,
    val saved: Boolean,
    @field:Relation(
        parentColumn = "episodeId",
        entityColumn = "episodeId"
    ) val releases: List<RoomRelease>
) : Indexable