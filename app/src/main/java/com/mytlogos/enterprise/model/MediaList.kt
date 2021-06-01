package com.mytlogos.enterprise.model

open class MediaList(
    val uuid: String,
    val listId: Int,
    val name: String,
    val medium: Int,
    val size: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val mediaList = other as MediaList
        return listId == mediaList.listId
    }

    override fun hashCode(): Int {
        return listId
    }
}