package com.mytlogos.enterprise.background.api.model

/**
 * API Model for MinList.
 */
class ClientMinList(val name: String, val medium: Int) {
    override fun toString(): String {
        return "ClientMinList{" +
                "name='" + name + '\'' +
                ", medium=" + medium +
                '}'
    }
}