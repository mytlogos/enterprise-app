package com.mytlogos.enterprise.model

import org.joda.time.DateTime

interface Release {
    val releaseDate: DateTime?
    val title: String?
    val url: String?
    val episodeId: Int
    val locked: Boolean
}