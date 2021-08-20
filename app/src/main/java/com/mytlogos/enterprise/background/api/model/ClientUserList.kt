package com.mytlogos.enterprise.background.api.model

/**
 * API Model for UserList.
 */
class ClientUserList(val id: Int, val name: String, val medium: Int) {
    override fun toString(): String {
        return "ClientMediaList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientUserList
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}