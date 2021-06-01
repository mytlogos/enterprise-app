package com.mytlogos.enterprise.model

import org.joda.time.DateTime

class Episode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    val partialIndex: Int,
    val totalIndex: Int,
    val readDate: DateTime?,
    val isSaved: Boolean
)