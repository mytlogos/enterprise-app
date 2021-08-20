package com.mytlogos.enterprise.model

class ExternalMediaList(
    uuid: String?,
    listId: Int,
    name: String?,
    medium: Int,
    val url: String?,
    size: Int
) : MediaList(uuid ?: "", listId, name ?: "", medium, size)