package com.mytlogos.enterprise.model

import androidx.annotation.IntDef
import kotlin.annotation.Retention

object MediumType {
    const val NOVEL = 0x1
    const val MANGA = 0x2
    const val ANIME = 0x4
    const val SERIES = 0x8
    const val ALL = ANIME or MANGA or NOVEL or SERIES
    const val TEXT = 0x1
    const val AUDIO = 0x2
    const val VIDEO = 0x4
    const val IMAGE = 0x8

    @kotlin.jvm.JvmStatic
    fun addMediumType(mediumType: Int, toAdd: Int): Int {
        return mediumType or toAdd
    }

    @kotlin.jvm.JvmStatic
    fun removeMediumType(mediumType: Int, toRemove: Int): Int {
        return mediumType and toRemove.inv()
    }

    fun toggleMediumType(mediumType: Int, toToggle: Int): Int {
        return mediumType xor toToggle
    }

    @kotlin.jvm.JvmStatic
    fun `is`(type: Int, toCheck: Int): Boolean {
        return type and toCheck == toCheck
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(value = [TEXT, IMAGE, AUDIO, VIDEO])
    annotation class Medium
}