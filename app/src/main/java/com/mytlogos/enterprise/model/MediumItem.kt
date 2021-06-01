package com.mytlogos.enterprise.model

import org.joda.time.DateTime

class MediumItem(
    override val title: String,
    override val mediumId: Int,
    override val author: String,
    override val artist: String,
    override val medium: Int,
    override val stateTL: Int,
    override val stateOrigin: Int,
    override val countryOfOrigin: String,
    override val languageOfOrigin: String,
    override val lang: String,
    override val series: String,
    override val universe: String,
    override val currentRead: Int,
    val currentReadEpisode: Int,
    val lastEpisode: Int,
    val lastUpdated: DateTime
) : Medium