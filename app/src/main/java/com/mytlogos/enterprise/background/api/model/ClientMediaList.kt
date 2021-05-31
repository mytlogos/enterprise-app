package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for List.
 */
class ClientMediaList(val userUuid: String, val id: Int, val name: String, val medium: Int, val items: IntArray?) {
    override fun toString(): String {
        return "ClientMediaList{" +
                "userUuid='" + userUuid + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", items=" + Arrays.toString(items) +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientMediaList
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}