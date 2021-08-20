package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for PureDisplayRelease.
 */
data class ClientRelease(
    val episodeId: Int,
    val title: String,
    val url: String,
    val isLocked: Boolean,
    val releaseDate: DateTime
)