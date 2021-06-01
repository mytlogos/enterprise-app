package com.mytlogos.enterprise.preferences

import android.content.SharedPreferences
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.model.MediumType.`is`
import java.util.*
import java.util.function.BiConsumer
import kotlin.math.min

class DownloadPreference internal constructor(preferences: SharedPreferences) :
    BasePreference(preferences) {
    private val map: Map<String, Set<BiConsumer<Int, Int>>> = HashMap()
    val isDownloadEnabled: Boolean
        get() = preferences.getBoolean("enable_download", true)

    private fun getDefaultMediumCountLimit(medium: Int): Int {
        var limit = Int.MAX_VALUE
        if (`is`(medium, MediumType.TEXT)) {
            limit = min(limit, 100)
        }
        if (`is`(medium, MediumType.IMAGE)) {
            limit = min(limit, 10)
        }
        if (`is`(medium, MediumType.VIDEO)) {
            limit = min(limit, 5)
        }
        if (`is`(medium, MediumType.IMAGE)) {
            limit = min(limit, 20)
        }
        require(limit != Int.MAX_VALUE) { "unknown mediumType: $medium" }
        return limit
    }

    fun getDownloadLimitCount(medium: Int): Int {
        return getInt("download-medium-$medium-count", getDefaultMediumCountLimit(medium))
    }

    fun getDownloadLimitSize(medium: Int): Int {
        return getInt("download-medium-$medium-space", IGNORE_INT_VALUE)
    }

    fun getMediumDownloadLimitCount(mediumId: Int): Int {
        return getInt("download-mediumId-$mediumId-count", IGNORE_INT_VALUE)
    }

    fun getMediumDownloadLimitSize(mediumId: Int): Int {
        return getInt("download-mediumId-$mediumId-space", IGNORE_INT_VALUE)
    }

    fun getListDownloadLimitCount(listId: Int): Int {
        return getInt("download-listId-$listId-count", IGNORE_INT_VALUE)
    }

    fun getListDownloadLimitSize(listId: Int): Int {
        return getInt("download-listId-$listId-space", IGNORE_INT_VALUE)
    }
}