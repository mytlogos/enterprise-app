package com.mytlogos.enterprise.model

class ExternalMediaListSetting(
    listId: Int,
    uuid: String,
    name: String,
    medium: Int,
    val url: String,
    size: Int,
    toDownload: Boolean
) : MediaListSetting(listId, uuid, name, medium, size, toDownload) {
    override val isNameMutable: Boolean
        get() = false

    override val isMediumMutable: Boolean
        get() = false
}