package com.mytlogos.enterprise.model

open class MediaListSetting(
    val listId: Int,
    val uuid: String,
    val name: String,
    val medium: Int,
    val size: Int,
    val toDownload: Boolean
) {
    val isToDownloadMutable: Boolean
        get() = true
    open val isNameMutable: Boolean
        get() = true
    open val isMediumMutable: Boolean
        get() = true
}