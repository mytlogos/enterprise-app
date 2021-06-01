package com.mytlogos.enterprise.model

class HomeStats(
    val unreadNewsCount: Int,
    val unreadChapterCount: Int,
    val readTodayCount: Int,
    val readTotalCount: Int,
    val internalLists: Int,
    val externalLists: Int,
    val externalUser: Int,
    val unusedMedia: Int,
    val audioMedia: Int,
    val videoMedia: Int,
    val textMedia: Int,
    val imageMedia: Int,
    val name: String
)