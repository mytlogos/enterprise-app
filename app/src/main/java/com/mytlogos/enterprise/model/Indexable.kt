package com.mytlogos.enterprise.model

interface Indexable {
    val totalIndex: Int
    val partialIndex: Int

    fun toCombiIndex(): Double {
        return "${totalIndex}.${partialIndex}".toDouble()
    }

    fun toIndexString(): String {
        return if (partialIndex > 0) {
            "$totalIndex.$partialIndex"
        } else {
            "$totalIndex"
        }
    }
}