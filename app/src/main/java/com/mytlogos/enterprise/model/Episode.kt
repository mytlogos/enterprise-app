package com.mytlogos.enterprise.model

import org.joda.time.DateTime

data class Episode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    override val partialIndex: Int,
    override val totalIndex: Int,
    val readDate: DateTime?,
    val isSaved: Boolean
) : Indexable